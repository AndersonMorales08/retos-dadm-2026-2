package com.example.triqui.features.triqui.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.serialization.saved
import com.example.triqui.features.triqui.domain.TriquiLogic
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope
import kotlin.time.Duration.Companion.milliseconds
import com.example.triqui.features.triqui.utils.SoundHelper
import kotlinx.coroutines.flow.update

class TriquiViewModel(private val state: SavedStateHandle) : ViewModel() {
    val triquiLogic: TriquiLogic = TriquiLogic()
    private var levelKeySaved: String by state.saved { "easy" }

    private val _level = MutableStateFlow(triquiLogic.getLevel(levelKeySaved))
    val level: StateFlow<TriquiLogic.DifficultyLevel> = _level.asStateFlow()

    private var uiStateSaved : TriquiUiState by state.saved { TriquiUiState() }
    private val _uiState = MutableStateFlow(uiStateSaved)
    val uiState: StateFlow<TriquiUiState> = _uiState.asStateFlow()


    var soundHelper: SoundHelper? = null

    private fun updateUiState(transform: (TriquiUiState) -> TriquiUiState) {
        _uiState.update { current ->
            val update = transform(current)
            uiStateSaved = update
            update
        }
    }
    fun updateLevel(level: String) {
        levelKeySaved = level
        _level.value = triquiLogic.getLevel(level)
    }
    fun onCellClicked(index: Int) {
        val currentState = _uiState.value

        // Bloquear clic si la casilla está ocupada, si es turno de la máquina o terminó el juego
        if (currentState.board[index] != null ||
            currentState.status != GameStatus.HumanTurn ||
            currentState.isGameOver
        ) return

        // 1. Movimiento del Jugador Humano
        makeMove(index, Player.HUMAN)
        soundHelper?.playHumanSound()

        if (checkGameStatus(Player.HUMAN)) return

        // 2. Turno del Ordenador (Retardo con Corrutina en lugar de Handler.postDelayed)
        updateUiState { it.copy(status = GameStatus.ComputerTurn) }

        viewModelScope.launch {
            delay(2500L.milliseconds) // 1 segundo de espera
            makeComputerMove()
        }
    }

    private fun makeComputerMove() {
        val currentState = _uiState.value
        if (currentState.isGameOver) return

        // Encontrar casillas vacías disponible
        val emptyIndices = currentState.board.mapIndexedNotNull { index, player ->
            if (player == null) index else null
        }

        if (emptyIndices.isNotEmpty()) {
            var move: Int = -1
            if (_level.value == TriquiLogic.DifficultyLevel.Easy) {
                move = triquiLogic.getRandomMove(emptyIndices)
            } else if (_level.value == TriquiLogic.DifficultyLevel.Hard) {
                move = triquiLogic.getWinningMove(currentState.board)

                if (move == -1) {
                    move = triquiLogic.getRandomMove(emptyIndices)
                }
            } else if (_level.value == TriquiLogic.DifficultyLevel.Expert) {
                move = triquiLogic.getWinningMove(currentState.board)

                if (move == -1) {
                    move = triquiLogic.getBlockingMove(currentState.board)
                }

                if (move == -1) {
                    move = triquiLogic.getRandomMove(emptyIndices)
                }
            }
            makeMove(move, Player.COMPUTER)
            soundHelper?.playComputerSound()
            checkGameStatus(Player.COMPUTER)
        }
    }

    private fun makeMove(index: Int, player: Player) {
        updateUiState { state ->
            val newBoard = state.board.toMutableList().apply { set(index, player) }
            state.copy(board = newBoard)
        }
    }

    private fun checkGameStatus(lastPlayer: Player): Boolean {
        val board = _uiState.value.board
        val wins = listOf(
            listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8), // Filas
            listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8), // Columnas
            listOf(0, 4, 8), listOf(2, 4, 6)                  // Diagonales
        )

        val hasWon = wins.any { trip -> trip.all { board[it] == lastPlayer } }

        if (hasWon) {
            updateUiState { state ->
                when (lastPlayer) {
                    Player.HUMAN -> state.copy(
                        status = GameStatus.Winner(Player.HUMAN),
                        humanWins = state.humanWins + 1,
                        isGameOver = true
                    )
                    Player.COMPUTER -> state.copy(
                        status = GameStatus.Winner(Player.COMPUTER),
                        computerWins = state.computerWins + 1,
                        isGameOver = true
                    )
                }
            }
            return true
        }

        if (board.none { it == null }) {
            updateUiState { state ->
                state.copy(
                    status = GameStatus.Tie,
                    ties = state.ties + 1,
                    isGameOver = true
                )
            }
            return true
        }

        if (lastPlayer == Player.COMPUTER) {
            updateUiState { it.copy(status = GameStatus.HumanTurn) }
        }
        return false
    }

    fun startNewGame() {
        updateUiState { state ->
            state.copy(
                board = List(9) { null },
                status = GameStatus.HumanTurn,
                isGameOver = false
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        soundHelper?.release()
    }
}