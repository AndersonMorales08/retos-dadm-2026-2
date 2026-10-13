package com.example.triqui.features.triqui.domain

import com.example.triqui.features.triqui.ui.Player

class TriquiLogic {
    enum class DifficultyLevel {Easy, Hard, Expert}

    fun getLevel(level: String): DifficultyLevel {
        val levDifficulty: DifficultyLevel = when (level) {
            "easy" -> DifficultyLevel.Easy
            "hard" -> DifficultyLevel.Hard
            "expert" -> DifficultyLevel.Expert
            else -> DifficultyLevel.Easy
        }

        return levDifficulty
    }

    fun getRandomMove(emptyIndices: List<Int>): Int {
        return emptyIndices.random()
    }

    fun getBlockingMove(board: List<Player?>): Int {
        val emptyIndices = board.mapIndexedNotNull { index, player ->
            if (player == null) index else null
        }

        if (emptyIndices.isNotEmpty()) {
            for (index in emptyIndices) {
                if (checkPosibleGameStatus(Player.HUMAN, board.toMutableList(), index)) return index
            }
        }

        return -1
    }
    fun getWinningMove(board: List<Player?>): Int {
        val emptyIndices = board.mapIndexedNotNull { index, player ->
            if (player == null) index else null
        }

        if (emptyIndices.isNotEmpty()) {
            for (index in emptyIndices) {
                if (checkPosibleGameStatus(Player.COMPUTER, board.toMutableList(), index)) return index
            }
        }

        return -1
    }

    fun checkPosibleGameStatus(lastPlayer: Player, board: MutableList<Player?>, index: Int): Boolean {
        var winning = false
        board[index] = lastPlayer
        val wins = listOf(
            listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8), // Filas
            listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8), // Columnas
            listOf(0, 4, 8), listOf(2, 4, 6)                  // Diagonales
        )

        for (win in wins) {
            if (board[win[0]] == board[win[1]] && board[win[1]] ==
                board[win[2]] && board[win[2]] == lastPlayer) winning = true
        }

        return winning
    }
}