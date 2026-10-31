package com.gemini.multiverse.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.gemini.multiverse.core.gemini.LiveSessionState
import com.gemini.multiverse.core.sensor.TiltData
import com.gemini.multiverse.ui.theme.JarvisElectricBlue
import com.gemini.multiverse.ui.theme.JarvisMagenta
import com.gemini.multiverse.ui.theme.JarvisPink
import com.gemini.multiverse.ui.theme.JarvisPurple
import kotlin.math.cos
import kotlin.math.sin

/**
 * Jarvis Futuristic 3D Plasma Orb (Matching Photo Screen 2):
 * Features:
 * - Glowing multi-layered cosmic plasma core
 * - Swirling organic light ribbons & filaments (electric blue, magenta, violet, soft pink)
 * - Device tilt 3D perspective refraction
 * - Real-time speech audio amplitude deformation
 */
@Composable
fun Lia3DOrb(
    modifier: Modifier = Modifier,
    size: Dp = 260.dp,
    amplitude: Float = 0f,
    tiltData: TiltData = TiltData(),
    sessionState: LiveSessionState = LiveSessionState.IDLE,
    auraColor: Color = JarvisMagenta,
    onOrbClick: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "OrbMotion")

    // Continuous rotation for plasma filaments
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "OrbRotation"
    )

    // Breathing pulse
    val breathingPulse by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "OrbBreathing"
    )

    // Secondary wave oscillation
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "OrbWave"
    )

    Box(
        modifier = modifier
            .size(size)
            .clickable { onOrbClick() },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val baseRadius = (this.size.minDimension / 2f) * 0.68f

            // Dynamic audio expansion
            val audioBoost = amplitude * baseRadius * 0.30f
            val currentRadius = (baseRadius * breathingPulse) + audioBoost

            // Tilt perspective offset
            val tiltOffsetX = -tiltData.roll * currentRadius * 0.35f
            val tiltOffsetY = tiltData.pitch * currentRadius * 0.35f
            val dynamicCenter = Offset(center.x + tiltOffsetX, center.y + tiltOffsetY)

            // 1. Volumetric Atmospheric Halo (Outermost)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        JarvisPurple.copy(alpha = 0.40f + (amplitude * 0.3f)),
                        JarvisMagenta.copy(alpha = 0.20f),
                        Color(0xFF381E72).copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = currentRadius * 1.70f
                ),
                radius = currentRadius * 1.70f,
                center = center
            )

            // 2. Translucent Plasma Core Sphere
            val coreGradient = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.85f),
                    JarvisPink.copy(alpha = 0.90f),
                    JarvisMagenta.copy(alpha = 0.75f),
                    JarvisPurple.copy(alpha = 0.60f),
                    Color(0xFF0F0721).copy(alpha = 0.92f)
                ),
                center = Offset(dynamicCenter.x - (currentRadius * 0.25f), dynamicCenter.y - (currentRadius * 0.25f)),
                radius = currentRadius * 1.15f
            )

            drawCircle(
                brush = coreGradient,
                radius = currentRadius * 0.90f,
                center = dynamicCenter
            )

            // 3. Swirling Organic Light Ribbons & Energy Filaments (Photo look!)
            val ribbonColors = listOf(
                JarvisElectricBlue.copy(alpha = 0.85f),
                JarvisMagenta.copy(alpha = 0.90f),
                JarvisPink.copy(alpha = 0.95f),
                Color(0xFFE879F9).copy(alpha = 0.80f),
                JarvisPurple.copy(alpha = 0.75f)
            )

            val ribbonCount = 5
            for (i in 0 until ribbonCount) {
                val rot = rotationAngle * (if (i % 2 == 0) 1f else -0.8f) + (i * (360f / ribbonCount))
                val waveOffset = sin(Math.toRadians((wavePhase + (i * 50)).toDouble())).toFloat() * 10f

                rotate(degrees = rot, pivot = dynamicCenter) {
                    val ovalWidth = currentRadius * (1.75f + (i * 0.08f)) + (amplitude * 25f)
                    val ovalHeight = currentRadius * (0.85f + (i * 0.12f) + (waveOffset * 0.02f))
                    val ovalTopLeft = Offset(dynamicCenter.x - (ovalWidth / 2f), dynamicCenter.y - (ovalHeight / 2f))

                    drawOval(
                        color = ribbonColors[i % ribbonColors.size],
                        topLeft = ovalTopLeft,
                        size = Size(ovalWidth, ovalHeight),
                        style = Stroke(width = (2.2f + (i * 0.4f)).dp.toPx())
                    )
                }
            }

            // 4. Subtle Interlaced Waveforms inside Core
            val innerPath = Path()
            val points = 36
            val stepAngle = 360f / points
            for (p in 0..points) {
                val rad = Math.toRadians((p * stepAngle + rotationAngle).toDouble())
                val rVariation = sin(rad * 3 + Math.toRadians(wavePhase.toDouble())).toFloat() * (6.dp.toPx() + amplitude * 14.dp.toPx())
                val r = (currentRadius * 0.75f) + rVariation
                val px = dynamicCenter.x + (cos(rad).toFloat() * r)
                val py = dynamicCenter.y + (sin(rad).toFloat() * r)
                if (p == 0) innerPath.moveTo(px, py) else innerPath.lineTo(px, py)
            }
            innerPath.close()

            drawPath(
                path = innerPath,
                color = JarvisPink.copy(alpha = 0.45f + (amplitude * 0.4f)),
                style = Stroke(width = 1.8.dp.toPx())
            )

            // 5. Specular Hotspot Glint
            drawCircle(
                color = Color.White.copy(alpha = 0.75f + (amplitude * 0.25f)),
                radius = currentRadius * 0.12f,
                center = Offset(dynamicCenter.x - (currentRadius * 0.35f), dynamicCenter.y - (currentRadius * 0.35f))
            )
        }
    }
}
