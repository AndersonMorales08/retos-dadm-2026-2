package com.example.triqui.core.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.triqui.features.triqui.ui.Player

@Composable
fun BoardView(
    board: List<Player?>,
    onCellClick: (Int) -> Unit,
    humanImageRes: Int,
    computerImageRes: Int,
    modifier: Modifier = Modifier
) {
    val neonGreen = Color(0xFF4ADE80)

    val humanBitmap = ImageBitmap.imageResource(id = humanImageRes)
    val computerBitmap = ImageBitmap.imageResource(id = computerImageRes)

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f) // Cuadrado de 1:1
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val cellWidth = size.width / 3f
                    val cellHeight = size.height / 3f
                    val col = (offset.x / cellWidth).toInt().coerceIn(0, 2)
                    val row = (offset.y / cellHeight).toInt().coerceIn(0, 2)
                    val position = row * 3 + col
                    onCellClick(position)
                }
            }
    ) {
        val cellWidth = size.width / 3f
        val cellHeight = size.height / 3f
        val gridWidthPx = 6.dp.toPx() // Grosor de las líneas del tablero

        // 1. Dibujar rejilla (Líneas verticales y horizontales)[cite: 1]
        for (i in 1..2) {
            // Líneas Verticales
            drawLine(
                color = neonGreen,
                start = Offset(cellWidth * i, 0f),
                end = Offset(cellWidth * i, size.height),
                strokeWidth = gridWidthPx
            )
            // Líneas Horizontales
            drawLine(
                color = neonGreen,
                start = Offset(0f, cellHeight * i),
                end = Offset(size.width, cellHeight * i),
                strokeWidth = gridWidthPx
            )
        }

        // 2. Dibujar imágenes X u O en cada celda[cite: 1]
        board.forEachIndexed { index, player ->
            if (player != null) {
                val col = index % 3
                val row = index / 3

                val left = (col * cellWidth).toInt()
                val top = (row * cellHeight).toInt()
                val bitmapToDraw = if (player == Player.HUMAN) humanBitmap else computerBitmap

                // Se dibuja escalado dentro del cuadrante
                drawImage(
                    image = bitmapToDraw,
                    dstOffset = IntOffset(left, top),
                    dstSize = IntSize(cellWidth.toInt(), cellHeight.toInt())
                )
            }
        }
    }
}