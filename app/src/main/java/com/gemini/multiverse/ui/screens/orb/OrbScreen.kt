package com.gemini.multiverse.ui.screens.orb

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.gemini.multiverse.GeminiMultiverseApp
import com.gemini.multiverse.core.actions.PhoneActionBridge
import com.gemini.multiverse.core.audio.AudioEngine
import com.gemini.multiverse.core.gemini.GeminiLiveClient
import com.gemini.multiverse.core.gemini.LiveSessionState
import com.gemini.multiverse.core.sensor.TiltSensorManager
import com.gemini.multiverse.core.service.VoiceSessionService
import com.gemini.multiverse.domain.personality.PersonalitiesCatalog
import com.gemini.multiverse.ui.components.Lia3DOrb
import com.gemini.multiverse.ui.navigation.Screen
import com.gemini.multiverse.ui.theme.JarvisBorder
import com.gemini.multiverse.ui.theme.JarvisElevated
import com.gemini.multiverse.ui.theme.JarvisHorizontalGradient
import com.gemini.multiverse.ui.theme.JarvisMagenta
import com.gemini.multiverse.ui.theme.JarvisPink
import com.gemini.multiverse.ui.theme.JarvisPrimaryGradient
import com.gemini.multiverse.ui.theme.JarvisPurple
import com.gemini.multiverse.ui.theme.JarvisSurface
import com.gemini.multiverse.ui.theme.JarvisVoid
import com.gemini.multiverse.ui.theme.RoseError
import com.gemini.multiverse.ui.theme.TextSnow
import com.gemini.multiverse.ui.theme.TextSubtle

/**
 * Screen 2: Real-Time Voice Assistant (Matching Photo UI)
 * Features:
 * - Real-Time vs Basic toggle capsule
 * - 3D Swirling Plasma Orb with audio & tilt reactions
 * - Live state feedback: "I'm Listening"
 * - Glowing multi-ring Purple Mic button & Audio / Keyboard controls
 */
@Composable
fun OrbScreen(navController: NavController? = null) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val container = remember { GeminiMultiverseApp.instance.container }
    val settingsState by container.settingsRepository.settingsState.collectAsState()

    val activePersona = remember(settingsState.selectedPersonalityId) {
        PersonalitiesCatalog.getById(settingsState.selectedPersonalityId)
    }

    var selectedMode by remember { mutableStateOf("realtime") } // "realtime" or "basic"

    val audioEngine = remember { AudioEngine() }
    val phoneActionBridge = remember { PhoneActionBridge(context) }
    val liveClient = remember {
        GeminiLiveClient(audioEngine) { name, args ->
            phoneActionBridge.executeTool(name, args)
        }
    }

    val tiltSensorManager = remember { TiltSensorManager(context) }
    val tiltData by tiltSensorManager.tiltData.collectAsState()

    val sessionState by liveClient.sessionState.collectAsState()
    val inputAmp by audioEngine.inputAmplitude.collectAsState()
    val outputAmp by audioEngine.outputAmplitude.collectAsState()
    val lastTranscript by liveClient.lastRecognizedText.collectAsState()
    val lastError by liveClient.lastErrorMessage.collectAsState()

    val visualAmplitude = if (sessionState == LiveSessionState.SPEAKING) outputAmp else inputAmp
    val isLive = sessionState != LiveSessionState.IDLE && sessionState != LiveSessionState.ERROR

    DisposableEffect(Unit) {
        tiltSensorManager.startListening()
        onDispose {
            tiltSensorManager.stopListening()
            liveClient.disconnect()
            VoiceSessionService.stop(context)
        }
    }

    // Mic Halo Pulse Animation
    val infiniteTransition = rememberInfiniteTransition(label = "MicHalo")
    val haloPulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "HaloPulse"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(JarvisVoid)
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(18.dp))

        // 1. Top Bar: Back Button + Segmented Toggle ["Real-Time", "Basic"] + Tune Settings
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circular Back Button
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(JarvisSurface)
                    .border(1.dp, JarvisBorder, CircleShape)
                    .clickable { navController?.popBackStack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextSnow,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Center Segmented Toggle: ["Real-Time", "Basic"]
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(22.dp))
                    .background(JarvisSurface)
                    .border(1.dp, JarvisBorder, RoundedCornerShape(22.dp))
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Real-Time Tab
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            if (selectedMode == "realtime") JarvisHorizontalGradient else androidx.compose.ui.graphics.SolidColor(Color.Transparent)
                        )
                        .clickable { selectedMode = "realtime" }
                        .padding(horizontal = 16.dp, vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Real-Time",
                        fontSize = 13.sp,
                        fontWeight = if (selectedMode == "realtime") FontWeight.Bold else FontWeight.Medium,
                        color = if (selectedMode == "realtime") Color.White else TextSubtle
                    )
                }

                // Basic Tab
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            if (selectedMode == "basic") JarvisHorizontalGradient else androidx.compose.ui.graphics.SolidColor(Color.Transparent)
                        )
                        .clickable { selectedMode = "basic" }
                        .padding(horizontal = 16.dp, vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Basic",
                        fontSize = 13.sp,
                        fontWeight = if (selectedMode == "basic") FontWeight.Bold else FontWeight.Medium,
                        color = if (selectedMode == "basic") Color.White else TextSubtle
                    )
                }
            }

            // Right Settings Slider Button
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(JarvisSurface)
                    .border(1.dp, JarvisBorder, CircleShape)
                    .clickable { navController?.navigate(Screen.Settings.route) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = "Settings",
                    tint = TextSnow,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 2. Center: 3D Swirling Plasma Orb
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Lia3DOrb(
                    size = 260.dp,
                    amplitude = visualAmplitude,
                    tiltData = tiltData,
                    sessionState = sessionState,
                    auraColor = JarvisMagenta,
                    onOrbClick = {
                        if (!isLive) {
                            val key = settingsState.apiKey
                            if (key.isNotBlank()) {
                                liveClient.connect(
                                    scope = scope,
                                    apiKey = key,
                                    personality = activePersona,
                                    languageMode = settingsState.selectedLanguage,
                                    useEchoCancellation = settingsState.isEchoCancellationEnabled
                                )
                                if (settingsState.isBackgroundServiceEnabled) {
                                    VoiceSessionService.start(context)
                                }
                            }
                        } else {
                            liveClient.disconnect()
                            VoiceSessionService.stop(context)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(22.dp))

                // Tag below Orb: "AI- Powered Voice Assistant"
                Text(
                    text = "AI- Powered Voice Assistant",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = JarvisPink,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Prompt description text
                Text(
                    text = if (lastTranscript.isNotBlank()) {
                        "\"$lastTranscript\""
                    } else {
                        "Backingound With Flowing Organic Shapes, Blending Deep Purple, Violet, Electric Blu, And Soft Pink Haze..."
                    },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFFCBD5E1),
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Live Status Text: "I'm Listening"
                Text(
                    text = when {
                        sessionState == LiveSessionState.ERROR && lastError.isNotBlank() -> "Error: $lastError"
                        sessionState == LiveSessionState.SPEAKING -> "Speaking..."
                        sessionState == LiveSessionState.LISTENING || isLive -> "I'm Listening"
                        sessionState == LiveSessionState.CONNECTING -> "Connecting..."
                        else -> "Tap Mic to Talk"
                    },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (sessionState == LiveSessionState.ERROR) RoseError else Color.White.copy(alpha = 0.9f)
                )
            }
        }

        // 3. Bottom Controls: Waveform Button + Massive Glowing Mic Button + Keyboard Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 28.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Circular Waveform Button
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(JarvisSurface)
                    .border(1.dp, JarvisBorder, CircleShape)
                    .clickable { /* Audio equalizer / waveform inspect */ },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.GraphicEq,
                    contentDescription = "Waveform",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Center: Massive Glowing Purple/Pink Microphone Button
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(90.dp)
            ) {
                // Outer glowing halo ring
                if (isLive) {
                    Box(
                        modifier = Modifier
                            .size(86.dp * haloPulse)
                            .clip(CircleShape)
                            .background(JarvisPurple.copy(alpha = 0.25f))
                    )
                }

                // Inner gradient mic button
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(
                            if (isLive) Brush.linearGradient(listOf(RoseError, JarvisMagenta))
                            else JarvisPrimaryGradient
                        )
                        .border(2.dp, Color.White.copy(alpha = 0.35f), CircleShape)
                        .clickable {
                            if (!isLive) {
                                val key = settingsState.apiKey
                                if (key.isNotBlank()) {
                                    liveClient.connect(
                                        scope = scope,
                                        apiKey = key,
                                        personality = activePersona,
                                        languageMode = settingsState.selectedLanguage,
                                        useEchoCancellation = settingsState.isEchoCancellationEnabled
                                    )
                                    if (settingsState.isBackgroundServiceEnabled) {
                                        VoiceSessionService.start(context)
                                    }
                                } else {
                                    navController?.navigate(Screen.Settings.route)
                                }
                            } else {
                                liveClient.disconnect()
                                VoiceSessionService.stop(context)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isLive) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = "Mic Control",
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            // Right: Circular Keyboard Button
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(JarvisSurface)
                    .border(1.dp, JarvisBorder, CircleShape)
                    .clickable { navController?.navigate(Screen.Chat.route) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Keyboard,
                    contentDescription = "Keyboard",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}
