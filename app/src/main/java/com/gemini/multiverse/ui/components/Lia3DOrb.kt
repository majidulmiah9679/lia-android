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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.gemini.multiverse.core.gemini.LiveSessionState
import com.gemini.multiverse.core.sensor.TiltData
import com.gemini.multiverse.ui.theme.CyanBioluminescent
import com.gemini.multiverse.ui.theme.IndigoBorder
import com.gemini.multiverse.ui.theme.MarigoldLight
import com.gemini.multiverse.ui.theme.MarigoldPrimary
import kotlin.math.cos
import kotlin.math.sin

/**
 * Lia's Interactive 3D Orb.
 * Uses real-time phone tilt coordinates for dynamic 3D specular light refraction
 * and audio RMS amplitude for surface wave deformation.
 */
@Composable
fun Lia3DOrb(
    modifier: Modifier = Modifier,
    size: Dp = 260.dp,
    amplitude: Float = 0f,
    tiltData: TiltData = TiltData(),
    sessionState: LiveSessionState = LiveSessionState.IDLE,
    auraColor: Color = MarigoldPrimary,
    onOrbClick: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "OrbMotion")

    // Idle breathing rotation
    val idlePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "IdleRotation"
    )

    // Pulse scale animation
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "OrbPulse"
    )

    Box(
        modifier = modifier
            .size(size)
            .clickable { onOrbClick() },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val baseRadius = (this.size.minDimension / 2f) * 0.70f

            // Dynamic radius affected by speech audio amplitude
            val audioExpansion = amplitude * baseRadius * 0.35f
            val currentRadius = (baseRadius * pulseScale) + audioExpansion

            // 1. Dynamic light source coordinate based on device tilt (Pitch & Roll)
            val lightOffsetX = center.x - (tiltData.roll * currentRadius * 0.55f)
            val lightOffsetY = center.y + (tiltData.pitch * currentRadius * 0.55f)
            val lightSource = Offset(lightOffsetX, lightOffsetY)

            // 2. Outer Ambient Plasma Glow (Layer 1)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        auraColor.copy(alpha = 0.35f + (amplitude * 0.4f)),
                        auraColor.copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = currentRadius * 1.55f
                ),
                radius = currentRadius * 1.55f,
                center = center
            )

            // 3. Concentric Orbiting Resonance Rings (Layer 2)
            val ringCount = 3
            for (i in 0 until ringCount) {
                val ringOffset = (i * 18.dp.toPx())
                val ringAngle = Math.toRadians((idlePhase + (i * 45)).toDouble())
                val ringTiltX = (cos(ringAngle) * 8.dp.toPx()).toFloat()
                val ringTiltY = (sin(ringAngle) * 8.dp.toPx()).toFloat()

                drawCircle(
                    color = auraColor.copy(alpha = 0.25f - (i * 0.06f) + (amplitude * 0.2f)),
                    radius = currentRadius + ringOffset + (amplitude * 20.dp.toPx()),
                    center = Offset(center.x + ringTiltX, center.y + ringTiltY),
                    style = Stroke(width = 1.5.dp.toPx())
                )
            }

            // 4. Main 3D Sphere Body with Specular Highlight Gradient (Layer 3)
            val sphereGradient = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.95f),                 // Specular hot spot
                    MarigoldLight.copy(alpha = 0.90f),               // Inner luminescence
                    auraColor.copy(alpha = 0.85f),                  // Primary aura
                    IndigoBorder.copy(alpha = 0.95f),               // Shadow terminator
                    Color(0xFF060612)                               // Deep shadowed back
                ),
                center = lightSource,
                radius = currentRadius * 1.25f
            )

            drawCircle(
                brush = sphereGradient,
                radius = currentRadius,
                center = center
            )

            // 5. State-specific Ripple Waves (Active Voice Feedback)
            if (sessionState == LiveSessionState.SPEAKING || sessionState == LiveSessionState.LISTENING) {
                val waveAlpha = (0.4f + amplitude * 0.6f).coerceIn(0f, 1f)
                drawCircle(
                    color = if (sessionState == LiveSessionState.SPEAKING) MarigoldLight else CyanBioluminescent,
                    radius = currentRadius * 0.92f,
                    center = center,
                    style = Stroke(width = (2.dp.toPx() + (amplitude * 4.dp.toPx())))
                )
            }
        }
    }
}
