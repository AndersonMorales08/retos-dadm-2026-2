package com.example.triqui.features.triqui.ui

import android.util.Log
import com.example.triqui.R
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.triqui.core.components.BoardView
import com.example.triqui.core.components.Menu
import com.example.triqui.features.triqui.utils.SoundHelper
import androidx.window.core.layout.WindowSizeClass
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun TriquiScreen(
    triquiViewModel: TriquiViewModel = viewModel(),
    windowSizeClass: WindowSizeClass = currentWindowAdaptiveInfo(supportLargeAndXLargeWidth = true).windowSizeClass

) {
    val showTopAppBar = windowSizeClass.isHeightAtLeastBreakpoint(WindowSizeClass.HEIGHT_DP_MEDIUM_LOWER_BOUND)
    val bgColor = Color(0xFF132F20)
    val neonGreen = Color(0xFF4ADE80)

    val context = LocalContext.current

    val uiState by triquiViewModel.uiState.collectAsState()
    val level by triquiViewModel.level.collectAsStateWithLifecycle()

    if (!showTopAppBar) {
        Log.d("Cambio de Orientación", "FUNCIONO $showTopAppBar")
        HorizontalTriquiScreen(triquiViewModel, bgColor, neonGreen, context,
            uiState, level)
    } else {

    // Manejo seguro del ciclo de vida del SoundPool
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { },
                modifier = Modifier.border(1.dp, Color.DarkGray, RoundedCornerShape(12.dp))
            ) {
                Text("🔊", color = neonGreen) // Reemplazo simple para icono de audio
            }

            Text("TRIQUI", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp)
            Menu(triquiViewModel)
        }

        val statusText = when (val status = uiState.status) {
            is GameStatus.HumanTurn -> "Tu turno"
            is GameStatus.ComputerTurn -> "Turno de Android..."
            is GameStatus.Tie -> "Empate!"
            is GameStatus.Winner -> if (status.winner == Player.HUMAN) "¡Ganaste!" else "Android ganó!"
        }

        Box(
            modifier = Modifier
                .border(1.dp, neonGreen, RoundedCornerShape(50))
                .padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            Text(statusText, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        Box(
            modifier = Modifier
                .border(1.dp, neonGreen, RoundedCornerShape(20))
                .padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            Text("$level", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }


        // Dibujo del Tablero Canvas[cite: 1]
        BoardView(
            board = uiState.board,
            onCellClick = { triquiViewModel.onCellClicked(it) },
            humanImageRes = R.drawable.icons8_x_96, //
            computerImageRes = R.drawable.icons8_circulo_96,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        )

        // Marcador de puntuación[cite: 1]
        Row(
            modifier = Modifier.fillMaxWidth()
                .border(1.dp, neonGreen, RoundedCornerShape(50))
                .padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Text(text = "Humano: ${uiState.humanWins}", color = Color.White)
            Text(text = "Empates: ${uiState.ties}", color = Color.White)
            Text(text = "Android: ${uiState.computerWins}", color = Color.White)
        }

        Button(
            onClick = { triquiViewModel.startNewGame() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = neonGreen)

        ) {
            Icon(Icons.Default.Refresh, contentDescription = "Reset", tint = bgColor)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Nuevo Juego",
                color = bgColor,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp
            )
        }
    }
    }
}