package com.example.triqui.features.triqui.ui

import kotlinx.serialization.Serializable

@Serializable
enum class Player { HUMAN, COMPUTER }

@Serializable
sealed interface GameStatus {
    @Serializable
    object HumanTurn : GameStatus

    @Serializable
    object ComputerTurn : GameStatus
    @Serializable
    data class Winner(val winner: Player) : GameStatus
    @Serializable
    object Tie : GameStatus
}

@Serializable
data class TriquiUiState(
    val board: List<Player?> = List(9) { null },
    val status: GameStatus = GameStatus.HumanTurn,
    val humanWins: Int = 0,
    val ties: Int = 0,
    val computerWins: Int = 0,
    val isGameOver: Boolean = false
)