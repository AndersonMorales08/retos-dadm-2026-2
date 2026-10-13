package com.example.triqui.core.components

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.triqui.features.triqui.ui.TriquiViewModel


@Composable
fun Menu(triquiViewModel: TriquiViewModel) {
    var showDialog by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    val contextLocal = LocalContext.current

    val bgColor = Color(0xFF132F20)
    val neonGreen = Color(0xFF4ADE80)

    Box(
        modifier = Modifier
            .padding(16.dp)
    ) {
        IconButton(onClick = { expanded = !expanded }, modifier = Modifier.border(1.dp, Color.DarkGray,
            RoundedCornerShape(12.dp))) {
            Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            Modifier.background(color = neonGreen)
        ) {
            DropdownMenuItem(
                text = { Text("New Game", color = bgColor) },
                leadingIcon = { Icon(Icons.Default.PlayArrow, contentDescription = null, tint =
                    bgColor) },
                onClick = { triquiViewModel.startNewGame() }
            )
            HorizontalDivider(color = bgColor)
            DropdownMenuItem(
                text = { Text("Difficulty", color = bgColor) },
                leadingIcon = { Icon(Icons.Default.Tune, contentDescription = null, tint =
                    bgColor)},
                onClick = { showDialog = true }
            )
            HorizontalDivider(color = bgColor)
            DropdownMenuItem(
                text = { Text("Quit", color = bgColor) },
                leadingIcon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription =
                    null, tint = bgColor)},
                onClick = { (contextLocal as? Activity)?.finishAffinity() }
            )
        }
    }

    if (showDialog) {
        DifficultyDialog(onDismissRequest = { showDialog = false }, triquiViewModel)
    }

}