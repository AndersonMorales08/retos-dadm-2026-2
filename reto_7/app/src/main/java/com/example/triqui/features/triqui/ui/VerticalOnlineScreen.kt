package com.example.triqui.features.triqui.ui

import android.content.Context
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.triqui.R
import com.example.triqui.core.components.BoardView
import com.example.triqui.core.components.Menu
import com.example.triqui.core.components.NameDialog
import com.example.triqui.core.repositories.Players
import com.example.triqui.core.repositories.TriquiOnlineRepository
import com.example.triqui.features.triqui.domain.TriquiLogic
import com.example.triqui.features.triqui.utils.SoundHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlin.time.Duration.Companion.milliseconds

val triquiOnlineRepository: TriquiOnlineRepository = TriquiOnlineRepository()

@Composable
fun VerticalOnlineScreen(
    triquiViewModel: TriquiViewModel = viewModel(),
    bgColor: Color,
    neonGreen: Color,
    context: Context,
    uiState: TriquiUiState,
    level: TriquiLogic.DifficultyLevel,
    onOfflineButtonClicked: () -> Unit,
    onMatchReady: (Players, String?) -> Unit = { _, _ -> },
    onlineViewModel: OnlineTriquiViewModel = viewModel()
) {

    var showNameDialog by remember { mutableStateOf(false) }
    var foundName by rememberSaveable { mutableStateOf("") }
    var inMatch by rememberSaveable { mutableStateOf(false) }

    // OBSERVER: solo escucha mientras estoy dentro de una partida.
    val players by remember<Flow<Players?>>(inMatch) {
        if (inMatch) triquiOnlineRepository.observePlayers() else flowOf(null)
    }.collectAsState(initial = null)

    val online by onlineViewModel.uiState.collectAsState()

    // Cuando están los dos jugadores empieza la partida; si el rival se va, se limpia.
    LaunchedEffect(players?.bothConnected) {
        val p = players
        if (p != null && p.bothConnected) {
            onlineViewModel.startMatch(triquiOnlineRepository.myFigure)
            onMatchReady(p, triquiOnlineRepository.myFigure)
        } else if (p != null) {
            onlineViewModel.stop()
        }
    }

    if (showNameDialog) {
        NameDialog(
            onDismiss = { showNameDialog = false },
            onSearch = { triquiOnlineRepository.joinMatch(it) },
            onFound = { foundName = it; inMatch = true; showNameDialog = false }
        )
    }



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
                onClick = {
                    if (inMatch) {
                        triquiOnlineRepository.leaveMatch(); onlineViewModel.stop()
                        inMatch = false
                    }
                    onOfflineButtonClicked()
                },
                modifier = Modifier.border(1.dp, Color.DarkGray, RoundedCornerShape(12.dp))
            ) {
                Icon(Icons.Default.Wifi, contentDescription = "Online", tint = Color.White)
            }

            Text("TRIQUI Online Pro", color = Color.White, fontWeight = FontWeight.ExtraBold,
                fontSize = 24.sp)
        }

        val statusText = when {
            inMatch && players?.bothConnected != true -> "Esperando a otro jugador…"
            inMatch -> when {
                online.winner != null ->
                    if (online.winner == online.myFigure) "¡Ganaste!" else "Ganó tu rival"
                online.tie -> "Empate!"
                online.myTurn -> "Tu turno (${online.myFigure})"
                else -> "Turno de tu rival…"
            }
            else -> when (val status = uiState.status) {
                is GameStatus.HumanTurn -> "Tu turno"
                is GameStatus.ComputerTurn -> "Turno de Android..."
                is GameStatus.Tie -> "Empate!"
                is GameStatus.Winner -> if (status.winner == Player.HUMAN) "¡Ganaste!" else "Android ganó!"
            }
        }

        Box(
            modifier = Modifier
                .border(1.dp, neonGreen, RoundedCornerShape(50))
                .padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            Text(statusText, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        if (!inMatch) {
            Box(
                modifier = Modifier
                    .border(1.dp, neonGreen, RoundedCornerShape(20))
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            ) {
                Text("$level", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }


        // Dibujo del Tablero Canvas[cite: 1]
        if (inMatch) {
            OnlineBoard(
                board = online.board,
                enabled = online.myTurn,
                neonGreen = neonGreen,
                xImageRes = R.drawable.icons8_x_96,
                oImageRes = R.drawable.icons8_circulo_96,
                onCellClick = { onlineViewModel.onCellClicked(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )
        } else {
            BoardView(
                board = uiState.board,
                onCellClick = { triquiViewModel.onCellClicked(it) },
                humanImageRes = R.drawable.icons8_x_96, //
                computerImageRes = R.drawable.icons8_circulo_96,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )
        }

        if (inMatch) {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .border(1.dp, neonGreen, RoundedCornerShape(50))
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(text = "${players?.player1?.name ?: "…"} (X)", color = Color.White)
                Text(text = "${players?.player2?.name ?: "…"} (O)", color = Color.White)
            }
        } else {
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
        }

        Button(
            onClick = {
                if (inMatch) {
                    triquiOnlineRepository.leaveMatch(); onlineViewModel.stop()
                    inMatch = false
                } else {
                    showNameDialog = true
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = neonGreen)

        ) {
            Icon(
                if (inMatch) Icons.Default.Close else Icons.Default.Search,
                contentDescription = if (inMatch) "Salir" else "Buscar",
                tint = bgColor
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (inMatch) "Salir de la partida" else "Buscar Partida",
                color = bgColor,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp
            )
        }
    }
}