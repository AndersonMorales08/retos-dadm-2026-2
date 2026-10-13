package com.example.triqui.core.repositories

import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.database.getValue
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/** Un jugador conectado: game/players/player_X. */
data class PlayerInfo(val name: String, val figure: String)

/** Jugadores actuales. Un jugador es null mientras no se haya unido. */
data class Players(val player1: PlayerInfo?, val player2: PlayerInfo?) {
    val bothConnected: Boolean get() = player1 != null && player2 != null
}

class TriquiOnlineRepository {
    val database = Firebase.database.reference

    private val playersRef get() = database.child("game").child("players")

    /** Lugar que me tocó al unirme ("player_1" o "player_2"); null si no estoy en una partida. */
    var mySlot: String? = null
        private set

    /** Mi figura según el lugar: "X" para player_1, "O" para player_2. */
    val myFigure: String?
        get() = when (mySlot) {
            "player_1" -> "X"
            "player_2" -> "O"
            else -> null
        }

    // ---------------------------------------------------------------------
    // Unirse a la partida
    // ---------------------------------------------------------------------

    /**
     * Misma firma y significado que antes: true = quedaste dentro (como
     * player_1 o player_2), false = la partida ya está llena.
     *
     * Ahora es una transacción: si dos jugadores buscan a la vez, no pueden
     * quedar los dos como el mismo jugador. También escribe `figure`
     * ("X" para player_1, "O" para player_2), que antes no se guardaba.
     */
    fun searchMatch(name: String, onResultado: (Boolean?) -> Unit, onError: (String) -> Unit) {
        var assignedSlot: String? = null
        // La transacción cubre todo `game` para que entrar y dejar el tablero
        // limpio (move/state) ocurra en una sola operación atómica.
        database.child("game").runTransaction(object : Transaction.Handler {
            override fun doTransaction(data: MutableData): Transaction.Result {
                // Si `data` viene vacío, p1 es null y se escribe player_1.
                // Si en realidad el servidor tenía datos, Firebase detecta la
                // diferencia y vuelve a ejecutar esto con el valor real.
                val players = data.child("players")
                val p1 = players.child("player_1").child("name").getValue(String::class.java)
                val p2 = players.child("player_2").child("name").getValue(String::class.java)

                val slot = when {
                    p1.isNullOrEmpty() -> "player_1"
                    p2.isNullOrEmpty() -> "player_2"
                    else -> return Transaction.abort() // partida llena
                }

                assignedSlot = slot
                players.child(slot).child("name").value = name
                players.child(slot).child("figure").value = if (slot == "player_1") "X" else "O"

                // Tablero limpio para la partida que empieza (sin jugadas viejas).
                data.child("move").value = listOf(-1, -1)
                data.child("state").value = ""
                return Transaction.success(data)
            }

            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                snapshot: DataSnapshot?
            ) {
                if (error != null) {
                    onError(error.message)
                    return
                }
                if (committed) {
                    mySlot = assignedSlot
                    // Si me desconecto (app cerrada, sin red), libero mi lugar.
                    assignedSlot?.let { playersRef.child(it).onDisconnect().removeValue() }
                }
                onResultado(committed)
            }
        })
    }

    /**
     * Versión suspend de searchMatch, para usarla como `onSearch` del NameDialog.
     * Devuelve false si la partida está llena; lanza excepción si hubo error
     * de Firebase (el NameDialog la trata igual que "no encontrada").
     */
    suspend fun joinMatch(name: String): Boolean = suspendCancellableCoroutine { cont ->
        searchMatch(
            name,
            onResultado = { if (cont.isActive) cont.resume(it == true) },
            onError = { if (cont.isActive) cont.resumeWithException(Exception(it)) }
        )
    }

    // ---------------------------------------------------------------------
    // OBSERVER de jugadores (reemplaza al observe(nombre) anterior)
    // ---------------------------------------------------------------------

    /**
     * Emite los jugadores cada vez que cambian. Cuando `bothConnected` pasa a
     * true, ya hay dos jugadores y la partida puede empezar. Sirve igual para
     * quien entró primero (espera) y para quien entró segundo (ya los ve a ambos).
     * El listener se elimina solo (awaitClose) cuando se deja de recolectar.
     */
    fun observePlayers(): Flow<Players> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                trySend(
                    Players(
                        player1 = snapshot.child("player_1").toPlayerInfo(),
                        player2 = snapshot.child("player_2").toPlayerInfo()
                    )
                )
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        playersRef.addValueEventListener(listener)
        awaitClose { playersRef.removeEventListener(listener) }
    }

    private fun DataSnapshot.toPlayerInfo(): PlayerInfo? {
        val name = child("name").getValue<String>()
        if (name.isNullOrEmpty()) return null
        return PlayerInfo(name, child("figure").getValue<String>() ?: "")
    }

    // ---------------------------------------------------------------------
    // Lecturas existentes (callbacks)
    // ---------------------------------------------------------------------

    /**
     * Devuelve una función para cancelar el listener (antes no se podía quitar).
     * Si no la usas, se comporta igual que antes.
     */
    fun readMove(onResultado: (List<Int>?) -> Unit, onError: (String) -> Unit): () -> Unit {
        val moveRef = database.child("game").child("move")
        val moveListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    onResultado(dataSnapshot.getValue<List<Int>>())
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                onError(databaseError.message)
            }
        }
        moveRef.addValueEventListener(moveListener)
        return { moveRef.removeEventListener(moveListener) }
    }

    /**
     * CORREGIDO: antes escuchaba game/move; ahora escucha game/state.
     * Devuelve una función para cancelar el listener.
     */
    fun readState(onResultado: (String?) -> Unit, onError: (String) -> Unit): () -> Unit {
        val stateRef = database.child("game").child("state")
        val stateListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    onResultado(dataSnapshot.getValue<String>())
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                onError(databaseError.message)
            }
        }
        stateRef.addValueEventListener(stateListener)
        return { stateRef.removeEventListener(stateListener) }
    }

    // ---------------------------------------------------------------------
    // Escrituras existentes
    // ---------------------------------------------------------------------

    fun writeMoveDone(move: List<Int>) {
        database.child("game").child("move").setValue(move)
    }

    fun writeState(state: String) {
        database.child("game").child("state").setValue(state)
    }

    /** Libera mi lugar en la partida (player_1 o player_2). */
    fun leaveMatch() {
        val slot = mySlot ?: return
        playersRef.child(slot).onDisconnect().cancel()
        playersRef.child(slot).removeValue()
        mySlot = null
    }

    /**
     * Deja la partida lista para empezar de nuevo: sin jugadores, tablero
     * limpio y estado vacío. Útil para pruebas y al terminar una partida.
     */
    fun resetMatch() {
        database.child("game").setValue(
            mapOf(
                "move" to listOf(-1, -1),
                "state" to ""
            )
        )
    }
}

/*
 * Uso en Compose (pantalla de espera):
 *
 * @Composable
 * fun SalaEsperaScreen(
 *     repo: TriquiOnlineRepository,
 *     onPartidaLista: (Players) -> Unit
 * ) {
 *     val players by remember { repo.observePlayers() }
 *         .collectAsStateWithLifecycle(initialValue = null)
 *
 *     LaunchedEffect(players?.bothConnected) {
 *         players?.takeIf { it.bothConnected }?.let(onPartidaLista) // inicia o alerta
 *     }
 *     // ...UI: CircularProgressIndicator + "Esperando a otro jugador…"
 * }
 *
 * Con el NameDialog:
 *
 * NameDialog(
 *     onDismiss = { showDialog = false },
 *     onSearch = { nombre -> repo.joinMatch(nombre) },
 *     onFound = { showDialog = false /* ir a SalaEsperaScreen */ }
 * )
 *
 * Dependencias: firebase-database (BoM), kotlinx-coroutines-play-services
 * ya no es necesaria aquí (no se usa await()), y lifecycle-runtime-compose
 * para collectAsStateWithLifecycle.
 */