package com.example.triqui_v2.features.triqui.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.triqui_v2.features.triqui.domain.TriquiLogic
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.triqui_v2.core.components.Menu

@Composable
fun TriquiScreen( triquiViewModel: TriquiViewModel = viewModel() ) {
    val boardState by triquiViewModel.boardState.collectAsStateWithLifecycle()
    val gameStatus by triquiViewModel.gameStatus.collectAsStateWithLifecycle()
    val isHumanTurn by triquiViewModel.isHumanTurn.collectAsStateWithLifecycle()
    val level by triquiViewModel.level.collectAsStateWithLifecycle()

    // Variables de UI basadas en el Mockup
    val bgColor = Color(0xFF132F20)
    val panelColor = Color(0xFF1A3D2A)
    val neonGreen = Color(0xFF4ADE80)
    val neonYellow = Color(0xFFD9F99D)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
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
            }
            Menu(triquiViewModel)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- SCORE BOARD ---

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

        Box(
            modifier = Modifier
                .border(1.dp, neonGreen, RoundedCornerShape(20))
                .padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            //val turnText = if (gameStatus != 0) "GAME OVER" else if (isHumanTurn) "PLAYER X'S " +
                "TURN" //else "PLAYER O'S TURN"
            Text("$level", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(64.dp))

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
                                    .clickable {triquiViewModel.onHumanPlay(index) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = cellValue.takeIf { it != TriquiLogic.OPEN_SPOT }?.toString() ?: "",
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

        Spacer(modifier = Modifier.weight(0.5f))

        val statusText = when (gameStatus) {
            1 -> "It's a tie!" //[cite: 1]
            2 -> "You won!" //[cite: 1]
            3 -> "Android won!" //[cite: 1]
            else -> "Matching is playing" // Texto del mockup simulado
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
                onClick = { triquiViewModel.resetMatch() },
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