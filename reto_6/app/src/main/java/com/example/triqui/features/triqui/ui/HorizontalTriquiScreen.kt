package com.example.triqui.features.triqui.ui

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.window.core.layout.WindowSizeClass
import com.example.triqui.R
import com.example.triqui.core.components.BoardView
import com.example.triqui.core.components.Menu
import com.example.triqui.features.triqui.domain.TriquiLogic
import com.example.triqui.features.triqui.utils.SoundHelper

@Composable
fun HorizontalTriquiScreen(
    triquiViewModel: TriquiViewModel = viewModel(),
    bgColor: Color,
    neonGreen: Color,
    context: Context,
    uiState: TriquiUiState,
    level: TriquiLogic.DifficultyLevel
) {
    DisposableEffect(Unit) {
        triquiViewModel.soundHelper = SoundHelper(
            context = context,
            swordResId = R.raw.sword, // Reemplazar por tus recursos
            swishResId = R.raw.swish  // Reemplazar por tus recursos
        )
        onDispose {
            triquiViewModel.soundHelper?.release()
        }
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { }, modifier = Modifier.border(1.dp, Color.DarkGray, RoundedCornerShape(12.dp))) {
                    Text("🔊", color = neonGreen) // Reemplazo simple para icono de audio
                }

                Text("TRIQUI", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp)
                Menu(triquiViewModel)
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                BoardView(
                    board = uiState.board,
                    onCellClick = { triquiViewModel.onCellClicked(it) },
                    humanImageRes = R.drawable.icons8_x_96, //
                    computerImageRes = R.drawable.icons8_circulo_96,
                    modifier = Modifier.fillMaxHeight()
                )
            }
        }


        val statusText = when (val status = uiState.status) {
            is GameStatus.HumanTurn -> "Tu turno"
            is GameStatus.ComputerTurn -> "Turno de Android..."
            is GameStatus.Tie -> "Empate!"
            is GameStatus.Winner -> if (status.winner == Player.HUMAN) "¡Ganaste!" else "Android ganó!"
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .border(1.dp, neonGreen, RoundedCornerShape(50))
                    .padding(horizontal = 20.dp, vertical = 6.dp)
            ) {
                Text(statusText, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            Box(
                modifier = Modifier
                    .border(1.dp, neonGreen, RoundedCornerShape(20))
                    .padding(horizontal = 20.dp, vertical = 6.dp)
            ) {
                Text("$level", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            Row(
                modifier = Modifier.fillMaxWidth()
                    .border(1.dp, neonGreen, RoundedCornerShape(50))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(text = "Humano: ${uiState.humanWins}", color = Color.White, fontSize = 12.sp)
                Text(text = "Empates: ${uiState.ties}", color = Color.White, fontSize = 12.sp)
                Text(text = "Android: ${uiState.computerWins}", color = Color.White, fontSize = 12.sp)
            }

            Button(
                onClick = { triquiViewModel.startNewGame() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = neonGreen)

            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Reset", tint = bgColor)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Nuevo Juego", color = bgColor, fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp)
            }
        }

    }
}