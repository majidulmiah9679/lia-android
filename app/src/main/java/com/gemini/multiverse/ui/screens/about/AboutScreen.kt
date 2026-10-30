package com.gemini.multiverse.ui.screens.about

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
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
fun AboutScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IndigoVoid)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Text(text = "About Lia", style = Typography.displayLarge, color = TextSnow)
        Text(text = "Version 1.0.0 • Gemini Multimodal Live Native Android Engine", style = Typography.bodyMedium, color = TextSubtle)

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, IndigoBorder, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = IndigoSurface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(text = "Lia Multiverse Architecture", style = Typography.titleLarge, color = MarigoldPrimary)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Built natively using Kotlin 2.0 and Jetpack Compose. Utilizes Gemini 2.0 Multimodal Live bidirectional WebSockets with 16kHz PCM streaming, full-duplex Acoustic Echo Cancellation, custom GPU shader audio-reactive tilt orb, and the in-app 3D Website Forge runtime.",
                    color = TextSnow,
                    fontSize = 14.sp,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(14.dp))
                Text(text = "Tech Stack:", style = Typography.titleLarge, color = CyanBioluminescent, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "• Jetpack Compose & Material 3\n• OkHttp WebSocket Bidi Stream\n• Android AudioTrack & AudioRecord\n• Three.js WebGL WebView Bridge\n• Android Accessibility Visual Agent", color = TextSubtle, fontSize = 13.sp)
            }
        }
    }
}
