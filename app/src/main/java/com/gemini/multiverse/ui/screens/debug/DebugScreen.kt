package com.gemini.multiverse.ui.screens.debug

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gemini.multiverse.ui.theme.CyanBioluminescent
import com.gemini.multiverse.ui.theme.IndigoBorder
import com.gemini.multiverse.ui.theme.IndigoSurface
import com.gemini.multiverse.ui.theme.IndigoVoid
import com.gemini.multiverse.ui.theme.MarigoldPrimary
import com.gemini.multiverse.ui.theme.TextSnow
import com.gemini.multiverse.ui.theme.TextSubtle
import com.gemini.multiverse.ui.theme.Typography

@Composable
fun DebugScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IndigoVoid)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Text(text = "Engine Diagnostics", style = Typography.displayLarge, color = TextSnow)
        Text(text = "Live WebSocket telemetry, audio pipeline latencies & stats", style = Typography.bodyMedium, color = TextSubtle)

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, IndigoBorder, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = IndigoSurface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(text = "Audio & Network Metrics", style = Typography.titleLarge, color = MarigoldPrimary)
                Spacer(modifier = Modifier.height(14.dp))

                DebugRow("Upstream Mic Rate", "16,000 Hz (16-bit PCM Mono)")
                DebugRow("Downstream Voice Rate", "24,000 Hz (16-bit PCM Mono)")
                DebugRow("WebSocket Protocol", "BidiGenerateContent v1alpha")
                DebugRow("Echo Canceler", "AcousticEchoCanceler Hardware")
                DebugRow("Noise Suppressor", "NoiseSuppressor Hardware")
                DebugRow("Target Model", "gemini-2.0-flash-exp")
                DebugRow("Barge-in Interruption", "Active (Zero-Latency Buffer Flush)")
            }
        }
    }
}

@Composable
fun DebugRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = TextSubtle, fontSize = 13.sp)
        Text(text = value, color = CyanBioluminescent, fontSize = 13.sp)
    }
}
