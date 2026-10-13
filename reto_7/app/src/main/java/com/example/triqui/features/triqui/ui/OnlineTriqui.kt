package com.example.triqui.features.triqui.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Estado de una partida online.
 * board: 9 casillas ("" = vacía, "X" u "O"); índice = fila * 3 + columna.
 */
data class OnlineUiState(
    val board: List<String> = List(9) { "" },
    val myFigure: String? = null,
    val turn: String = "X",          // X (player_1) siempre empieza
    val winner: String? = null,      // "X" | "O" | null
    val tie: Boolean = false,
    val started: Boolean = false
) {
    val finished: Boolean get() = winner != null || tie
    val myTurn: Boolean get() = started && !finished && turn == myFigure
}

/**
 * Lógica de la partida contra otra persona. Es independiente del
 * TriquiViewModel (que sigue siendo el juego contra Android).
 *
 * Protocolo en Firebase: game/move = [fila, columna] de la última jugada
 * ([-1, -1] = ninguna). Cada app aplica su jugada en local y la escribe; la
 * app rival la recibe por readMove y la aplica. El eco de mi propia jugada se
 * ignora porque esa casilla ya está ocupada.
 */
class OnlineTriquiViewModel : ViewModel() {

    private val repo = triquiOnlineRepository

    private val _ui = MutableStateFlow(OnlineUiState())
    val uiState: StateFlow<OnlineUiState> = _ui.asStateFlow()

    private var stopListening: (() -> Unit)? = null

    /** Empieza la partida cuando ya hay dos jugadores. Es idempotente (rotación de pantalla). */
    fun startMatch(myFigure: String?) {
        if (myFigure == null || _ui.value.started) return
        _ui.value = OnlineUiState(myFigure = myFigure, started = true)

        stopListening?.invoke()
        stopListening = repo.readMove(
            onResultado = { move -> if (move != null) applyRemoteMove(move) },
            onError = { }
        )
    }

    /** Toque en una casilla: solo vale en mi turno y en una casilla vacía. */
    fun onCellClicked(index: Int) {
        val s = _ui.value
        if (!s.myTurn || index !in 0..8 || s.board[index].isNotEmpty()) return
        place(index)
        repo.writeMoveDone(listOf(index / 3, index % 3))
    }

    /** Sale de la partida: deja de escuchar y limpia el estado local. */
    fun stop() {
        stopListening?.invoke()
        stopListening = null
        _ui.value = OnlineUiState()
    }

    override fun onCleared() {
        stopListening?.invoke()
    }

    private fun applyRemoteMove(move: List<Int>) {
        val s = _ui.value
        if (!s.started || s.finished || move.size < 2) return
        val row = move[0]
        val col = move[1]
        if (row !in 0..2 || col !in 0..2) return      // [-1, -1] = sin jugada
        val index = row * 3 + col
        if (s.board[index].isNotEmpty()) return       // eco de mi propia jugada
        if (s.turn == s.myFigure) return              // no es el turno del rival
        place(index)
    }

    private fun place(index: Int) {
        val s = _ui.value
        val board = s.board.toMutableList().also { it[index] = s.turn }
        val winner = winnerOf(board)
        _ui.value = s.copy(
            board = board,
            winner = winner,
            tie = winner == null && board.none { it.isEmpty() },
            turn = if (s.turn == "X") "O" else "X"
        )
    }

    private fun winnerOf(b: List<String>): String? {
        val lines = listOf(
            listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8),   // filas
            listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8),   // columnas
            listOf(0, 4, 8), listOf(2, 4, 6)                     // diagonales
        )
        return lines.firstOrNull { (a, c, d) ->
            b[a].isNotEmpty() && b[a] == b[c] && b[c] == b[d]
        }?.let { b[it[0]] }
    }
}

/** Tablero 3x3 simple para la partida online. */
@Composable
fun OnlineBoard(
    board: List<String>,
    enabled: Boolean,
    neonGreen: Color,
    xImageRes: Int,
    oImageRes: Int,
    onCellClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        repeat(3) { row ->
            Row(modifier = Modifier.fillMaxWidth()) {
                repeat(3) { col ->
                    val index = row * 3 + col
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .border(1.dp, neonGreen)
                            .clickable(enabled = enabled && board[index].isEmpty()) {
                                onCellClick(index)
                            }
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        when (board[index]) {
                            "X" -> Image(painterResource(xImageRes), contentDescription = "X")
                            "O" -> Image(painterResource(oImageRes), contentDescription = "O")
                        }
                    }
                }
            }
        }
    }
}