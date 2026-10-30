package com.gemini.multiverse.ui.screens.orb

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
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gemini.multiverse.GeminiMultiverseApp
import com.gemini.multiverse.core.actions.PhoneActionBridge
import com.gemini.multiverse.core.audio.AudioEngine
import com.gemini.multiverse.core.gemini.GeminiLiveClient
import com.gemini.multiverse.core.gemini.LiveSessionState
import com.gemini.multiverse.core.sensor.TiltSensorManager
import com.gemini.multiverse.core.service.VoiceSessionService
import com.gemini.multiverse.domain.personality.PersonalitiesCatalog
import com.gemini.multiverse.ui.components.Lia3DOrb
import com.gemini.multiverse.ui.theme.CyanBioluminescent
import com.gemini.multiverse.ui.theme.EmeraldOnline
import com.gemini.multiverse.ui.theme.IndigoBorder
import com.gemini.multiverse.ui.theme.IndigoElevated
import com.gemini.multiverse.ui.theme.IndigoSurface
import com.gemini.multiverse.ui.theme.IndigoVoid
import com.gemini.multiverse.ui.theme.MarigoldGlow
import com.gemini.multiverse.ui.theme.MarigoldPrimary
import com.gemini.multiverse.ui.theme.RoseError
import com.gemini.multiverse.ui.theme.TextDim
import com.gemini.multiverse.ui.theme.TextSnow
import com.gemini.multiverse.ui.theme.TextSubtle
import com.gemini.multiverse.ui.theme.Typography
import kotlinx.coroutines.launch

@Composable
fun OrbScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val container = remember { GeminiMultiverseApp.instance.container }
    val settingsState by container.settingsRepository.settingsState.collectAsState()

    val activePersona = remember(settingsState.selectedPersonalityId) {
        PersonalitiesCatalog.getById(settingsState.selectedPersonalityId)
    }

    // Audio Engine & Gemini Live Client
    val audioEngine = remember { AudioEngine() }
    val phoneActionBridge = remember { PhoneActionBridge(context) }
    val liveClient = remember {
        GeminiLiveClient(audioEngine) { name, args ->
            phoneActionBridge.executeTool(name, args)
        }
    }

    // Tilt Sensor Manager
    val tiltSensorManager = remember { TiltSensorManager(context) }
    val tiltData by tiltSensorManager.tiltData.collectAsState()

    val sessionState by liveClient.sessionState.collectAsState()
    val inputAmp by audioEngine.inputAmplitude.collectAsState()
    val outputAmp by audioEngine.outputAmplitude.collectAsState()
    val lastTranscript by liveClient.lastRecognizedText.collectAsState()

    // Determine combined visual amplitude
    val visualAmplitude = if (sessionState == LiveSessionState.SPEAKING) outputAmp else inputAmp

    DisposableEffect(Unit) {
        tiltSensorManager.startListening()
        onDispose {
            tiltSensorManager.stopListening()
            audioEngine.release()
            liveClient.disconnect()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IndigoVoid)
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // Top Status Header & Persona Pill
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // State Badge
            val statusColor = when (sessionState) {
                LiveSessionState.CONNECTED -> EmeraldOnline
                LiveSessionState.SPEAKING -> MarigoldPrimary
                LiveSessionState.LISTENING -> CyanBioluminescent
                LiveSessionState.CONNECTING -> MarigoldGlow
                LiveSessionState.ERROR -> RoseError
                LiveSessionState.IDLE -> TextDim
            }

            val statusText = when (sessionState) {
                LiveSessionState.CONNECTED -> "Online"
                LiveSessionState.SPEAKING -> "Lia bol rahi hai..."
                LiveSessionState.LISTENING -> "Lia sun rahi hai..."
                LiveSessionState.CONNECTING -> "Connecting..."
                LiveSessionState.ERROR -> "Check API Key"
                LiveSessionState.IDLE -> "Standby"
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(IndigoSurface)
                    .border(1.dp, IndigoBorder, RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(statusColor)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = statusText,
                    color = statusColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Active Persona Indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(IndigoSurface)
                    .border(1.dp, activePersona.auraColor.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(text = activePersona.avatarEmoji, fontSize = 14.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = activePersona.name,
                    color = TextSnow,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.weight(0.8f))

        // The 3D Interactive Audio & Tilt Reactive Orb
        Lia3DOrb(
            size = 280.dp,
            amplitude = visualAmplitude,
            tiltData = tiltData,
            sessionState = sessionState,
            auraColor = activePersona.auraColor,
            onOrbClick = {
                if (sessionState == LiveSessionState.IDLE || sessionState == LiveSessionState.ERROR) {
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

        Spacer(modifier = Modifier.height(28.dp))

        // Voice Subtitle / Speech Display
        Text(
            text = if (lastTranscript.isNotBlank()) "\"$lastTranscript\"" else "Tap Orb to speak in Hindi, Hinglish or English",
            style = Typography.bodyLarge,
            color = if (lastTranscript.isNotBlank()) TextSnow else TextSubtle,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        // Action Buttons & Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val isLive = sessionState != LiveSessionState.IDLE && sessionState != LiveSessionState.ERROR

            Button(
                onClick = {
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
                },
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .height(54.dp),
                shape = RoundedCornerShape(27.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isLive) RoseError else MarigoldPrimary,
                    contentColor = if (isLive) TextSnow else IndigoVoid
                )
            ) {
                Icon(
                    imageVector = if (isLive) Icons.Default.Stop else Icons.Default.Mic,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (isLive) "End Voice Session" else "Start Talking to Lia",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}
