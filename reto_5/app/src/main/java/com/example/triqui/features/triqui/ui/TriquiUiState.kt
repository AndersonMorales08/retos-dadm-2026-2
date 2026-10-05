package com.example.triqui.features.triqui.ui

enum class Player { HUMAN, COMPUTER }

sealed interface GameStatus {
    object HumanTurn : GameStatus
    object ComputerTurn : GameStatus
    data class Winner(val winner: Player) : GameStatus
    object Tie : GameStatus
}

data class TriquiUiState(
    val board: List<Player?> = List(9) { null },
    val status: GameStatus = GameStatus.HumanTurn,
    val humanWins: Int = 0,
    val ties: Int = 0,
    val computerWins: Int = 0,
    val isGameOver: Boolean = false
)