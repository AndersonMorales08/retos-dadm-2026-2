package com.example.triqui_v2.features.triqui.domain

import android.R
import android.util.Log
import androidx.compose.material3.Switch

class TriquiLogic {
    enum class DifficultyLevel {Easy, Hard, Expert}

    var mDifDifficultyLevel = DifficultyLevel.Easy

    companion object {
        const val HUMAN_PLAYER = 'X' //
        const val COMPUTER_PLAYER = 'O' //
        const val OPEN_SPOT = ' ' //
        const val BOARD_SIZE = 9 //
    }

    var board = CharArray(BOARD_SIZE) { OPEN_SPOT }

    fun getLevel(level: String): DifficultyLevel {
        var levDifficulty: DifficultyLevel = when (level) {
            "easy" -> DifficultyLevel.Easy
            "hard" -> DifficultyLevel.Hard
            "expert" -> DifficultyLevel.Expert
            else -> DifficultyLevel.Easy
        }

        return levDifficulty
    }

    fun clearBoard() { //
        for (i in 0 until Companion.BOARD_SIZE) {
            board[i] = Companion.OPEN_SPOT
        }
    }

    fun setMove(player: Char, location: Int, boardGame: CharArray = this.board) {
        if (location in 0 until Companion.BOARD_SIZE && boardGame[location] == Companion.OPEN_SPOT) {
            boardGame[location] = player
        }
    }

    fun checkForWinner(boardGame: CharArray = this.board): Int { //
        val winLines = listOf(
            listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8), // Filas
            listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8), // Columnas
            listOf(0, 4, 8), listOf(2, 4, 6)                   // Diagonales
        )
        for (line in winLines) {
            if (boardGame[line[0]] != OPEN_SPOT &&
                boardGame[line[0]] == boardGame[line[1]] &&
                boardGame[line[1]] == boardGame[line[2]]) {
                return if (boardGame[line[0]] == HUMAN_PLAYER) 2 else 3
            }
        }
        if (boardGame.none { it == OPEN_SPOT }) return 1 // Empate (1)[cite: 1]
        return 0 // Sin ganador aún (0)[cite: 1]
    }

    fun getComputerMove(): Int {
        var move: Int = -1

        if (mDifDifficultyLevel == DifficultyLevel.Easy) {
            // Lógica simplificada: Busca un espacio abierto aleatorio para evitar complejidad extensa aquí
            move = getRandomMove()
        } else if (mDifDifficultyLevel == DifficultyLevel.Hard) {
            Log.d("Difficulty", "$mDifDifficultyLevel")
            move = getWinningMove()
            Log.d("Winning", "Move: $move")
            if (move == -1) {
                move = getRandomMove()
                Log.d("Random", "Move: $move")
            }
        } else if (mDifDifficultyLevel == DifficultyLevel.Expert) {
            Log.d("Difficulty", "$mDifDifficultyLevel")
            move = getWinningMove()
            Log.d("Winning:", "Move: $move")
            if (move == -1) {
                move = getBlockingMove()
                Log.d("Blocking", "Move: $move")
            }
            if (move == -1) {
                move = getRandomMove()
                Log.d("Random", "Move: $move")
            }
        }

        return move
    }

    private fun getRandomMove(): Int {
        val availableSpots = board.indices.filter { board[it] == OPEN_SPOT }
        return if (availableSpots.isNotEmpty()) availableSpots.random() else -1
    }

    private fun getBlockingMove(): Int {
        var copyBoard: CharArray = this.board.copyOf()
        var availableSpots = copyBoard.indices.filter { copyBoard[it] == OPEN_SPOT }

        for (spot in availableSpots) {
            copyBoard = this.board.copyOf()
            availableSpots = copyBoard.indices.filter { copyBoard[it] == OPEN_SPOT }
            this.setMove(HUMAN_PLAYER, spot, copyBoard)
            if (this.checkForWinner(copyBoard) == 2) return spot
            this.setMove(OPEN_SPOT, spot, copyBoard)
        }
        return -1
    }
    private fun getWinningMove(): Int {
        var copyBoard: CharArray = this.board.copyOf()
        var availableSpots = copyBoard.indices.filter { copyBoard[it] == OPEN_SPOT }

        for (spot in availableSpots) {
            copyBoard = this.board.copyOf()
            availableSpots = copyBoard.indices.filter { copyBoard[it] == OPEN_SPOT }
            this.setMove(COMPUTER_PLAYER, spot, copyBoard)
            if (this.checkForWinner(copyBoard) == 3) return spot
            this.setMove(OPEN_SPOT, spot, copyBoard)
        }
        return -1
    }
}