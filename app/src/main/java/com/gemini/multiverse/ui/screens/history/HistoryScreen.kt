package com.gemini.multiverse.ui.screens.history

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gemini.multiverse.ui.theme.CyanBioluminescent
import com.gemini.multiverse.ui.theme.EmeraldOnline
import com.gemini.multiverse.ui.theme.IndigoBorder
import com.gemini.multiverse.ui.theme.IndigoSurface
import com.gemini.multiverse.ui.theme.IndigoVoid
import com.gemini.multiverse.ui.theme.MarigoldPrimary
import com.gemini.multiverse.ui.theme.TextDim
import com.gemini.multiverse.ui.theme.TextSnow
import com.gemini.multiverse.ui.theme.TextSubtle
import com.gemini.multiverse.ui.theme.Typography

data class HistoryEntry(
    val title: String,
    val detail: String,
    val timeAgo: String,
    val icon: ImageVector,
    val iconTint: androidx.compose.ui.graphics.Color
)

@Composable
fun HistoryScreen() {
    val entries = listOf(
        HistoryEntry("3D Forge: Cyber Cafe Website", "Generated Three.js site with coffee particle vortex", "10m ago", Icons.Default.Language, CyanBioluminescent),
        HistoryEntry("Phone Action: Mummy ko call", "Contact dialed via ACTION_DIAL", "45m ago", Icons.Default.Phone, EmeraldOnline),
        HistoryEntry("Voice Session: Dost Persona", "Duration: 8m 42s in Hinglish", "2h ago", Icons.Default.Mic, MarigoldPrimary)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IndigoVoid)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Text(text = "Activity History", style = Typography.displayLarge, color = TextSnow)
        Text(text = "Past voice calls, generated sites, and executed phone actions", style = Typography.bodyMedium, color = TextSubtle)

        Spacer(modifier = Modifier.height(20.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(entries) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, IndigoBorder, RoundedCornerShape(14.dp)),
                    colors = CardDefaults.cardColors(containerColor = IndigoSurface),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = item.icon, contentDescription = null, tint = item.iconTint, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(text = item.title, style = Typography.titleLarge, color = TextSnow, fontSize = 15.sp)
                                Text(text = item.detail, color = TextSubtle, fontSize = 12.sp)
                            }
                        }
                        Text(text = item.timeAgo, color = TextDim, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
