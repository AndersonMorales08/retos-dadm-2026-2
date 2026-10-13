package com.example.triqui.features.triqui.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.serialization.saved
import androidx.lifecycle.viewModelScope
import com.example.triqui.core.repositories.TriquiRepository
import com.example.triqui.features.triqui.domain.TriquiLogic
import com.example.triqui.features.triqui.utils.SoundHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class TriquiViewModel(
    private val state: SavedStateHandle,
    private val repository: TriquiRepository,
) : ViewModel() {
    val triquiLogic: TriquiLogic = TriquiLogic()

    // 1. Guardar la clave del nivel ("easy", "hard", "expert") en SavedStateHandle
    private var levelKeySaved: String by state.saved { "easy" }

    // Restore del nivel desde la clave guardada
    private val _level = MutableStateFlow(triquiLogic.getLevel(levelKeySaved))
    val level: StateFlow<TriquiLogic.DifficultyLevel> = _level.asStateFlow()

    // 2. Estado principal guardado
    private var uiStateSaved: TriquiUiState by state.saved { TriquiUiState() }

    private val _uiState = MutableStateFlow(uiStateSaved)
    val uiState: StateFlow<TriquiUiState> = _uiState.asStateFlow()

    var soundHelper: SoundHelper? = null

    init {
        // Carga inicial desde DataStore si la app arranca desde cero (memoria limpia)
        viewModelScope.launch {
            if (_uiState.value == TriquiUiState()) {
                val persistedState = repository.uiStateFlow.first()
                _uiState.value = persistedState
                uiStateSaved = persistedState
            }
        }
    }

    private fun updateUiState(transform: (TriquiUiState) -> TriquiUiState) {
        _uiState.update { current ->
            val updated = transform(current)
            uiStateSaved = updated

            viewModelScope.launch {
                repository.saveUiState(updated)
            }

            updated
        }
    }

    // Actualiza el flujo del nivel y persiste la clave
    fun updateLevel(levelKey: String) {
        levelKeySaved = levelKey
        _level.value = triquiLogic.getLevel(levelKey)

        viewModelScope.launch {
            repository.saveLevel(levelKey)
        }
    }

    fun onCellClicked(index: Int) {
        val currentState = _uiState.value

        if (currentState.board[index] != null ||
            currentState.status != GameStatus.HumanTurn ||
            currentState.isGameOver
        ) return

        makeMove(index, Player.HUMAN)
        soundHelper?.playHumanSound()

        if (checkGameStatus(Player.HUMAN)) return

        updateUiState { it.copy(status = GameStatus.ComputerTurn) }

        viewModelScope.launch {
            delay(2500L.milliseconds)
            makeComputerMove()
        }
    }

    private fun makeComputerMove() {
        val currentState = _uiState.value
        if (currentState.isGameOver) return

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
            listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8),
            listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8),
            listOf(0, 4, 8), listOf(2, 4, 6)
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