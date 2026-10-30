package com.gemini.multiverse.ui.modifiers

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.gemini.multiverse.ui.theme.MarigoldPrimary

/**
 * 3D Depth & Spatial Elevation modifier.
 * Applies subtle hardware-accelerated rotational perspective and glowing gradient cast shadows.
 */
fun Modifier.depth3D(
    elevation: Dp = 8.dp,
    rotationX: Float = 0f,
    rotationY: Float = 0f,
    glowColor: Color = MarigoldPrimary.copy(alpha = 0.25f)
): Modifier = this
    .graphicsLayer {
        this.shadowElevation = elevation.toPx()
        this.rotationX = rotationX
        this.rotationY = rotationY
        this.cameraDistance = 16f * density
    }
    .drawBehind {
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(glowColor, Color.Transparent),
                center = Offset(size.width / 2f, size.height),
                radius = size.width * 0.75f
            )
        )
    }
