package com.example.triqui.features.triqui.domain

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume

/*
 * Estructura en Realtime Database:
 *
 * game
 *  ├─ move:    [0: -1, 1: -1]
 *  ├─ players
 *  │   ├─ player_1: { figure: "X", name: "David" }
 *  │   └─ player_2: { figure: "O", name: "Anderson" }   <- no existe hasta que alguien se une
 *  └─ state:   ""
 */

// Valores que escribe este código en `state`. Ajústalos a los que use tu juego.
private const val ESTADO_ESPERANDO = "waiting"
private const val ESTADO_JUGANDO = "playing"

data class Jugador(val name: String, val figure: String)

data class Juego(
    val player1: Jugador?,
    val player2: Jugador?,
    val state: String,
    val move: List<Int>
)

/** Se lee a mano (sin mapeo automático) para no depender de nombres con "_". */
private fun DataSnapshot.toJugador(): Jugador? {
    val name = child("name").getValue(String::class.java)
    if (name.isNullOrBlank()) return null
    return Jugador(name, child("figure").getValue(String::class.java) ?: "")
}

private fun DataSnapshot.toJuego(): Juego? {
    if (!exists()) return null
    return Juego(
        player1 = child("players/player_1").toJugador(),
        player2 = child("players/player_2").toJugador(),
        state = child("state").getValue(String::class.java) ?: "",
        move = child("move").children.map { (it.value as? Number)?.toInt() ?: -1 }
    )
}

class PartidaRepository(
    root: DatabaseReference = Firebase.database.reference
) {
    private val game = root.child("game")

    /**
     * El anfitrión crea la partida: reemplaza todo el nodo `game`,
     * así que borra a player_2 y deja el tablero limpio.
     */
    suspend fun crear(hostName: String) {
        game.setValue(
            mapOf(
                "players" to mapOf(
                    "player_1" to mapOf("name" to hostName, "figure" to "X")
                ),
                "move" to listOf(-1, -1),
                "state" to ESTADO_ESPERANDO
            )
        ).await()
    }

    /**
     * OBSERVER: emite el estado completo del juego cada vez que algo cambia.
     * El listener se elimina solo (awaitClose) cuando se deja de recolectar.
     */
    fun observar(): Flow<Juego?> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                trySend(snapshot.toJuego())
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        game.addValueEventListener(listener)
        awaitClose { game.removeEventListener(listener) }
    }

    /**
     * El invitado se une como player_2 ("O") con una transacción, para que
     * dos jugadores no puedan entrar a la vez. Devuelve false si no hay una
     * partida esperando (no existe player_1) o si player_2 ya está ocupado.
     * Sirve como `onSearch` del NameDialog.
     */
    suspend fun unirse(nombre: String): Boolean =
        suspendCancellableCoroutine { cont ->
            game.runTransaction(object : Transaction.Handler {
                override fun doTransaction(data: MutableData): Transaction.Result {
                    // La primera ejecución puede traer null aunque el nodo exista
                    // (aún no llegan datos del servidor). Se devuelve sin cambios
                    // y Firebase reintenta con el valor real.
                    if (data.value == null) return Transaction.success(data)

                    val hayHost = !data.child("players/player_1/name")
                        .getValue(String::class.java).isNullOrBlank()
                    val ocupado = !data.child("players/player_2/name")
                        .getValue(String::class.java).isNullOrBlank()
                    if (!hayHost || ocupado) return Transaction.abort()

                    data.child("players/player_2/name").value = nombre
                    data.child("players/player_2/figure").value = "O"
                    data.child("state").value = ESTADO_JUGANDO
                    return Transaction.success(data)
                }

                override fun onComplete(
                    error: DatabaseError?,
                    committed: Boolean,
                    snapshot: DataSnapshot?
                ) {
                    val unido = error == null && committed &&
                            snapshot?.child("players/player_2/name")
                                ?.getValue(String::class.java) == nombre
                    if (cont.isActive) cont.resume(unido)
                }
            })
        }
}

/** Pantalla del anfitrión: espera y reacciona cuando entra el segundo jugador. */
@Composable
fun SalaEsperaScreen(
    onJugadorConectado: (Jugador) -> Unit,   // aquí inicias la partida o muestras la alerta
    repo: PartidaRepository = remember { PartidaRepository() }
) {
    val juego by remember { repo.observar() }
        .collectAsStateWithLifecycle(initialValue = null)

    val rival = juego?.player2
    LaunchedEffect(rival?.name) {
        if (rival != null) onJugadorConectado(rival)
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
        Text("Esperando a otro jugador…")
    }
}

/*
 * Conexión con el NameDialog (lado del invitado):
 *
 * NameDialog(
 *     onDismiss = { showDialog = false },
 *     onSearch = { nombre -> repo.unirse(nombre) },
 *     onFound = { nombre -> showDialog = false /* ir a la pantalla de juego */ }
 * )
 *
 * Lado del anfitrión: repo.crear("David") y luego mostrar SalaEsperaScreen.
 *
 * Dependencias (build.gradle.kts del módulo app):
 *   implementation(platform("com.google.firebase:firebase-bom:<última versión>"))
 *   implementation("com.google.firebase:firebase-database")
 *   implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:<versión>")
 *   implementation("androidx.lifecycle:lifecycle-runtime-compose:<versión>")
 */
