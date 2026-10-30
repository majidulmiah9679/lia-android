package com.gemini.multiverse.ui.screens.privacy

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gemini.multiverse.ui.theme.EmeraldOnline
import com.gemini.multiverse.ui.theme.IndigoBorder
import com.gemini.multiverse.ui.theme.IndigoSurface
import com.gemini.multiverse.ui.theme.IndigoVoid
import com.gemini.multiverse.ui.theme.TextSnow
import com.gemini.multiverse.ui.theme.TextSubtle
import com.gemini.multiverse.ui.theme.Typography

@Composable
fun PrivacyScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IndigoVoid)
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Text(text = "Privacy & Security", style = Typography.displayLarge, color = TextSnow)
        Text(text = "Zero-telemetry architecture and local encrypted security guarantee", style = Typography.bodyMedium, color = TextSubtle)

        Spacer(modifier = Modifier.height(20.dp))

        PrivacyCard(
            title = "Hardware-Backed AES-256 GCM",
            description = "Your Gemini API key and licenses are stored in the Android Keystore using EncryptedSharedPreferences with AES-256-SIV and AES-256-GCM encryption."
        )

        Spacer(modifier = Modifier.height(14.dp))

        PrivacyCard(
            title = "Direct Client-to-Gemini Connection",
            description = "All WebSocket voice streams and text requests go directly between your phone and Google's official Gemini API endpoints. No intermediate servers or telemetry."
        )

        Spacer(modifier = Modifier.height(14.dp))

        PrivacyCard(
            title = "Microphone Echo Guard Privacy",
            description = "Microphone data is only processed in-memory during active voice sessions and never written to disk or recorded without your active trigger."
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun PrivacyCard(title: String, description: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, IndigoBorder, RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = IndigoSurface),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = Typography.titleLarge, color = EmeraldOnline, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = description, color = TextSnow, fontSize = 13.sp, lineHeight = 20.sp)
        }
    }
}
