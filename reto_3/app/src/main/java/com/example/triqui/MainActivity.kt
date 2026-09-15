package com.example.triqui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// --- 1. LÓGICA DEL JUEGO TRADUCIDA DEL PDF ---
class TicTacToeGame {
    companion object {
        const val HUMAN_PLAYER = 'X' //
        const val COMPUTER_PLAYER = 'O' //
        const val OPEN_SPOT = ' ' //
        const val BOARD_SIZE = 9 //
    }

    // Arreglo de caracteres para representar el tablero
    var board = CharArray(BOARD_SIZE) { OPEN_SPOT }

    fun clearBoard() { //
        for (i in 0 until BOARD_SIZE) {
            board[i] = OPEN_SPOT
        }
    }

    fun setMove(player: Char, location: Int) { //
        if (location in 0 until BOARD_SIZE && board[location] == OPEN_SPOT) {
            board[location] = player
        }
    }

    fun checkForWinner(): Int { //
        val winLines = listOf(
            listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8), // Filas
            listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8), // Columnas
            listOf(0, 4, 8), listOf(2, 4, 6)                   // Diagonales
        )
        for (line in winLines) {
            if (board[line[0]] != OPEN_SPOT &&
                board[line[0]] == board[line[1]] &&
                board[line[1]] == board[line[2]]) {
                return if (board[line[0]] == HUMAN_PLAYER) 2 else 3
            }
        }
        if (board.none { it == OPEN_SPOT }) return 1 // Empate (1)[cite: 1]
        return 0 // Sin ganador aún (0)[cite: 1]
    }

    fun getComputerMove(): Int { //[cite: 1]
        // Lógica simplificada: Busca un espacio abierto aleatorio para evitar complejidad extensa aquí
        val availableSpots = board.indices.filter { board[it] == OPEN_SPOT }
        return if (availableSpots.isNotEmpty()) availableSpots.random() else -1
    }
}

// --- 2. INTERFAZ DE USUARIO (JETPACK COMPOSE) ---
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TriquiApp()
        }
    }
}

@Composable
fun TriquiApp() {
    val game = remember { TicTacToeGame() }
    var boardState by remember { mutableStateOf(game.board.toList()) }
    var gameStatus by remember { mutableStateOf(0) } // 0 = Jugando, 1 = Empate, 2 = X, 3 = O
    var isHumanTurn by remember { mutableStateOf(true) }

    // Variables de UI basadas en el Mockup
    val bgColor = Color(0xFF132F20)
    val panelColor = Color(0xFF1A3D2A)
    val neonGreen = Color(0xFF4ADE80)
    val neonYellow = Color(0xFFD9F99D)

    val coroutineScope = rememberCoroutineScope()

    fun updateBoard() {
        boardState = game.board.toList()
        gameStatus = game.checkForWinner()
    }

    fun onHumanPlay(index: Int) {
        if (gameStatus == 0 && isHumanTurn && game.board[index] == TicTacToeGame.OPEN_SPOT) {
            game.setMove(TicTacToeGame.HUMAN_PLAYER, index)
            isHumanTurn = false
            updateBoard()

            // Turno de la computadora
            if (gameStatus == 0) {
                coroutineScope.launch {
                    delay(500) // Simular "pensamiento"
                    val move = game.getComputerMove()
                    if (move != -1) {
                        game.setMove(TicTacToeGame.COMPUTER_PLAYER, move)
                        isHumanTurn = true
                        updateBoard()
                    }
                }
            }
        }
    }

    fun resetMatch() {
        game.clearBoard()
        isHumanTurn = true
        updateBoard()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- HEADER ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { }, modifier = Modifier.border(1.dp, Color.DarkGray, RoundedCornerShape(12.dp))) {
                Text("🔊", color = neonGreen) // Reemplazo simple para icono de audio
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("TRIQUI", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                    modifier = Modifier.background(neonGreen, RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("PRO", color = bgColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
            IconButton(onClick = { }, modifier = Modifier.border(1.dp, Color.DarkGray, RoundedCornerShape(12.dp))) {
                Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- SCORE BOARD ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color.DarkGray, RoundedCornerShape(24.dp))
                .background(panelColor, RoundedCornerShape(24.dp))
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("PLAYER X", color = Color.Gray, fontSize = 12.sp)
                Text("2", color = neonGreen, fontWeight = FontWeight.Bold, fontSize = 24.sp)
            }
            Box(
                modifier = Modifier
                    .background(bgColor, RoundedCornerShape(50))
                    .border(1.dp, Color.DarkGray, RoundedCornerShape(50))
                    .padding(12.dp)
            ) {
                Text("VS", color = Color.White, fontSize = 12.sp)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("PLAYER O", color = Color.Gray, fontSize = 12.sp)
                Text("1", color = neonYellow, fontWeight = FontWeight.Bold, fontSize = 24.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- TURN INDICATOR ---
        Box(
            modifier = Modifier
                .border(1.dp, neonGreen, RoundedCornerShape(50))
                .padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            val turnText = if (gameStatus != 0) "GAME OVER" else if (isHumanTurn) "PLAYER X'S TURN" else "PLAYER O'S TURN"
            Text("● $turnText", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- GAME BOARD ---
        Box(
            modifier = Modifier
                .border(1.dp, Color.DarkGray, RoundedCornerShape(24.dp))
                .padding(12.dp)
        ) {
            Column {
                for (row in 0 until 3) {
                    Row {
                        for (col in 0 until 3) {
                            val index = row * 3 + col
                            val cellValue = boardState[index]
                            Box(
                                modifier = Modifier
                                    .size(90.dp)
                                    .padding(4.dp)
                                    .background(Color(0xFF28543B), RoundedCornerShape(16.dp))
                                    .border(1.dp, neonGreen.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                                    .clickable { onHumanPlay(index) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = cellValue.takeIf { it != TicTacToeGame.OPEN_SPOT }?.toString() ?: "",
                                    color = if (cellValue == 'X') neonGreen else neonYellow,
                                    fontSize = 48.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // --- BOTTOM SECTION ---
        Text("Match Round 4 of 5", color = Color.Gray, fontSize = 14.sp)
        val statusText = when (gameStatus) {
            1 -> "It's a tie!" //[cite: 1]
            2 -> "You won!" //[cite: 1]
            3 -> "Android won!" //[cite: 1]
            else -> "X can win in 1 move!" // Texto del mockup simulado
        }
        Text(statusText, color = neonGreen, fontWeight = FontWeight.Bold, fontSize = 14.sp)

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            IconButton(
                onClick = { },
                modifier = Modifier
                    .size(56.dp)
                    .background(panelColor, RoundedCornerShape(16.dp))
            ) {
                Icon(Icons.Default.Home, contentDescription = "Home", tint = Color.White)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Button(
                onClick = { resetMatch() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = neonGreen)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Reset", tint = bgColor)
                Spacer(modifier = Modifier.width(8.dp))
                Text("RESET MATCH", color = bgColor, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}