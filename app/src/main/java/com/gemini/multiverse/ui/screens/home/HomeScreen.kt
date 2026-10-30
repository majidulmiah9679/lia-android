package com.gemini.multiverse.ui.screens.home

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.gemini.multiverse.GeminiMultiverseApp
import com.gemini.multiverse.domain.personality.PersonalitiesCatalog
import com.gemini.multiverse.ui.modifiers.depth3D
import com.gemini.multiverse.ui.navigation.Screen
import com.gemini.multiverse.ui.theme.CyanBioluminescent
import com.gemini.multiverse.ui.theme.EmeraldOnline
import com.gemini.multiverse.ui.theme.IndigoBorder
import com.gemini.multiverse.ui.theme.IndigoElevated
import com.gemini.multiverse.ui.theme.IndigoSurface
import com.gemini.multiverse.ui.theme.IndigoVoid
import com.gemini.multiverse.ui.theme.MarigoldGlow
import com.gemini.multiverse.ui.theme.MarigoldPrimary
import com.gemini.multiverse.ui.theme.TextSnow
import com.gemini.multiverse.ui.theme.TextSubtle
import com.gemini.multiverse.ui.theme.Typography

@Composable
fun HomeScreen(navController: NavController) {
    val container = remember { GeminiMultiverseApp.instance.container }
    val settingsState by container.settingsRepository.settingsState.collectAsState()
    val activePersona = remember(settingsState.selectedPersonalityId) {
        PersonalitiesCatalog.getById(settingsState.selectedPersonalityId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IndigoVoid)
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Welcome Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Namaste! ✨",
                    style = Typography.displayLarge,
                    color = TextSnow
                )
                Text(
                    text = "Lia is ready to assist you",
                    style = Typography.bodyMedium,
                    color = TextSubtle
                )
            }

            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(activePersona.auraColor.copy(alpha = 0.2f))
                    .border(1.5.dp, activePersona.auraColor, CircleShape)
                    .clickable { navController.navigate(Screen.Personalities.route) },
                contentAlignment = Alignment.Center
            ) {
                Text(text = activePersona.avatarEmoji, fontSize = 22.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 3D Hero Card: Start Voice Session
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .depth3D(elevation = 12.dp, glowColor = MarigoldPrimary.copy(alpha = 0.35f))
                .border(1.5.dp, MarigoldPrimary, RoundedCornerShape(20.dp))
                .clip(RoundedCornerShape(20.dp))
                .clickable { navController.navigate(Screen.Voice.route) },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = IndigoElevated)
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(MarigoldPrimary)
                        .border(2.dp, MarigoldGlow, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = null,
                        tint = IndigoVoid,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = "Talk to Lia (Gemini Live)",
                        style = Typography.titleLarge,
                        color = TextSnow
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Real-time voice in Hindi, Hinglish & English",
                        color = MarigoldGlow,
                        fontSize = 13.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Quick Grid: Forge, Chat, Personas, Orb Style
        Text(
            text = "Multiverse Hub",
            style = Typography.titleLarge,
            color = TextSnow
        )

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            HubCard(
                title = "3D Forge",
                subtitle = "Build websites",
                icon = Icons.Default.Language,
                iconTint = CyanBioluminescent,
                modifier = Modifier.weight(1f),
                onClick = { navController.navigate(Screen.Forge.route) }
            )

            HubCard(
                title = "Chat Console",
                subtitle = "Text with Lia",
                icon = Icons.Default.Chat,
                iconTint = MarigoldPrimary,
                modifier = Modifier.weight(1f),
                onClick = { navController.navigate(Screen.Chat.route) }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            HubCard(
                title = "10 Personas",
                subtitle = "Switch style",
                icon = Icons.Default.Person,
                iconTint = EmeraldOnline,
                modifier = Modifier.weight(1f),
                onClick = { navController.navigate(Screen.Personalities.route) }
            )

            HubCard(
                title = "Orb Style",
                subtitle = "Shader themes",
                icon = Icons.Default.ColorLens,
                iconTint = CyanBioluminescent,
                modifier = Modifier.weight(1f),
                onClick = { navController.navigate(Screen.OrbStyle.route) }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Utility Links
        Text(
            text = "System & Security",
            style = Typography.titleLarge,
            color = TextSnow
        )

        Spacer(modifier = Modifier.height(12.dp))

        SystemLinkRow("Permissions & Hardware Access", Icons.Default.Security) {
            navController.navigate(Screen.Permissions.route)
        }
        SystemLinkRow("Conversation & Action History", Icons.Default.FlashOn) {
            navController.navigate(Screen.History.route)
        }
        SystemLinkRow("Zero-Telemetry Privacy Guarantee", Icons.Default.Security) {
            navController.navigate(Screen.Privacy.route)
        }
        SystemLinkRow("Engine & WebSocket Diagnostics (Debug)", Icons.Default.FlashOn) {
            navController.navigate(Screen.Debug.route)
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun HubCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .depth3D(elevation = 8.dp, glowColor = iconTint.copy(alpha = 0.2f))
            .border(1.dp, IndigoBorder, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = IndigoSurface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = title, style = Typography.titleLarge, color = TextSnow, fontSize = 16.sp)
            Text(text = subtitle, color = TextSubtle, fontSize = 12.sp)
        }
    }
}

@Composable
fun SystemLinkRow(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(IndigoSurface)
            .border(1.dp, IndigoBorder, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = MarigoldPrimary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = title, color = TextSnow, fontSize = 14.sp)
        }
        Text(text = "→", color = TextSubtle, fontSize = 16.sp)
    }
}
