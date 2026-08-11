package com.example.util

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.abs

object QrEngine {

    /**
     * Renders a clean QR code matrix visually for a student's ID/QR token.
     */
    @Composable
    fun QrCodeView(
        data: String,
        modifier: Modifier = Modifier,
        size: Dp = 160.dp,
        primaryColor: Color = Color(0xFF0F172A),
        backgroundColor: Color = Color.White
    ) {
        val matrixSize = 15
        val hash = abs(data.hashCode())

        Box(
            modifier = modifier
                .size(size)
                .clip(RoundedCornerShape(12.dp))
                .background(backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(size - 24.dp)) {
                val cellSize = this.size.width / matrixSize

                // Draw background
                drawRect(color = backgroundColor, size = this.size)

                for (row in 0 until matrixSize) {
                    for (col in 0 until matrixSize) {
                        // Position marker squares in 3 corners
                        val isCornerMarker = (row < 4 && col < 4) ||
                                (row < 4 && col >= matrixSize - 4) ||
                                (row >= matrixSize - 4 && col < 4)

                        val isBorder = (row == 0 || row == 3 || row == matrixSize - 1 || row == matrixSize - 4) &&
                                (col <= 3 || col >= matrixSize - 4) ||
                                (col == 0 || col == 3 || col == matrixSize - 1 || col == matrixSize - 4) &&
                                (row <= 3 || row >= matrixSize - 4)

                        val isCenterDot = (row == 1.5.toInt() && col == 1.5.toInt()) ||
                                (row == 1.5.toInt() && col == matrixSize - 2) ||
                                (row == matrixSize - 2 && col == 1.5.toInt())

                        val bitSeed = (hash + row * 31 + col * 17) % 7
                        val isDataPixel = bitSeed < 3

                        if (isCornerMarker) {
                            if (isBorder || isCenterDot || (row in 1..2 && col in 1..2) ||
                                (row in 1..2 && col in matrixSize - 3..matrixSize - 2) ||
                                (row in matrixSize - 3..matrixSize - 2 && col in 1..2)
                            ) {
                                drawRect(
                                    color = primaryColor,
                                    topLeft = Offset(col * cellSize, row * cellSize),
                                    size = Size(cellSize, cellSize)
                                )
                            }
                        } else if (isDataPixel) {
                            drawRect(
                                color = primaryColor,
                                topLeft = Offset(col * cellSize, row * cellSize),
                                size = Size(cellSize, cellSize)
                            )
                        }
                    }
                }
            }
        }
    }
}
