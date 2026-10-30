package com.gemini.multiverse.ui.screens.orbstyle

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gemini.multiverse.GeminiMultiverseApp
import com.gemini.multiverse.ui.theme.CyanBioluminescent
import com.gemini.multiverse.ui.theme.EmeraldOnline
import com.gemini.multiverse.ui.theme.IndigoBorder
import com.gemini.multiverse.ui.theme.IndigoElevated
import com.gemini.multiverse.ui.theme.IndigoSurface
import com.gemini.multiverse.ui.theme.IndigoVoid
import com.gemini.multiverse.ui.theme.MarigoldLight
import com.gemini.multiverse.ui.theme.MarigoldPrimary
import com.gemini.multiverse.ui.theme.TextSnow
import com.gemini.multiverse.ui.theme.TextSubtle
import com.gemini.multiverse.ui.theme.Typography
import kotlinx.coroutines.launch

data class OrbStylePreset(
    val id: String,
    val name: String,
    val description: String,
    val primaryColor: Color,
    val secondaryColor: Color
)

@Composable
fun OrbStyleScreen() {
    val container = remember { GeminiMultiverseApp.instance.container }
    val settingsState by container.settingsRepository.settingsState.collectAsState()
    val scope = rememberCoroutineScope()

    val styles = listOf(
        OrbStylePreset("marigold_neon", "Marigold Sunshine (Default)", "Golden solar aura with amber specular highlights", MarigoldPrimary, MarigoldLight),
        OrbStylePreset("cyber_cyan", "Cyberpunk Cyan", "Bioluminescent electric cyan matrix look", CyanBioluminescent, Color(0xFF0088FF)),
        OrbStylePreset("aurora", "Aurora Borealis", "Mystic emerald and violet dancing curtain", EmeraldOnline, Color(0xFF9D00FF)),
        OrbStylePreset("plasma", "Cosmic Plasma", "Deep interstellar ultraviolet with magenta edge flare", Color(0xFFFF007F), Color(0xFF7000FF))
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IndigoVoid)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Text(text = "3D Orb Styles", style = Typography.displayLarge, color = TextSnow)
        Text(text = "Customize the GPU shader shader look and color aura", style = Typography.bodyMedium, color = TextSubtle)

        Spacer(modifier = Modifier.height(20.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            items(styles) { style ->
                val isSelected = settingsState.orbStyle == style.id
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) style.primaryColor else IndigoBorder,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable {
                            scope.launch {
                                container.settingsRepository.setOrbStyle(style.id)
                            }
                        },
                    colors = CardDefaults.cardColors(containerColor = if (isSelected) IndigoElevated else IndigoSurface)
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(style.primaryColor)
                                    .border(2.dp, style.secondaryColor, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(text = style.name, style = Typography.titleLarge, color = TextSnow, fontSize = 15.sp)
                                Text(text = style.description, color = TextSubtle, fontSize = 12.sp)
                            }
                        }

                        if (isSelected) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = style.primaryColor, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
    }
}
