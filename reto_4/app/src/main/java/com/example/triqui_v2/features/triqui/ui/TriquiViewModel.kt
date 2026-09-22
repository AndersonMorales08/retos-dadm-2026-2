package com.example.triqui_v2.features.triqui.ui

import android.R
import androidx.lifecycle.ViewModel
import com.example.triqui_v2.features.triqui.domain.TriquiLogic
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.example.triqui_v2.features.triqui.domain.TriquiLogic.Companion
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope
import kotlin.time.Duration.Companion.milliseconds

class TriquiViewModel : ViewModel() {
    val triquiLogic: TriquiLogic = TriquiLogic()

    private val _boardState = MutableStateFlow(triquiLogic.board.toList())
    val boardState: StateFlow<List<Char>> = _boardState.asStateFlow()

    private val _gameStatus = MutableStateFlow(0)
    val gameStatus: StateFlow<Int> = _gameStatus.asStateFlow()

    private val _isHumanTurn = MutableStateFlow(true)
    val isHumanTurn: StateFlow<Boolean> = _isHumanTurn.asStateFlow()

    private val _level = MutableStateFlow(triquiLogic.getLevel("easy"))
    val level: StateFlow<TriquiLogic.DifficultyLevel> = _level.asStateFlow()

    fun updateBoardAndStatus() {
        _boardState.value = triquiLogic.board.toList()
        _gameStatus.value = triquiLogic.checkForWinner()
    }

    fun updateLevel(level: String) {
        triquiLogic.mDifDifficultyLevel = triquiLogic.getLevel(level)
        _level.value = triquiLogic.mDifDifficultyLevel
    }

    fun onHumanPlay(index: Int) {
        if (_gameStatus.value == 0 && _isHumanTurn.value && triquiLogic.board[index] == Companion
            .OPEN_SPOT) {
            triquiLogic.setMove(Companion.HUMAN_PLAYER, index)
            _isHumanTurn.value = false
            updateBoardAndStatus()

            // Turno de la computadora
            if (_gameStatus.value == 0) {
                viewModelScope.launch {
                    delay(500.milliseconds) // Simular "pensamiento"
                    val move = triquiLogic.getComputerMove()
                    if (move != -1) {
                        triquiLogic.setMove(Companion.COMPUTER_PLAYER, move)
                        _isHumanTurn.value = true
                        updateBoardAndStatus()
                    }
                }
            }
        }
    }

    fun resetMatch() {
        triquiLogic.clearBoard()
        _isHumanTurn.value = true
        updateBoardAndStatus()
    }
}