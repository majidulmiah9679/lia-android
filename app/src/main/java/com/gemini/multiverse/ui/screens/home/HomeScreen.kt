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
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Videocam
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.gemini.multiverse.GeminiMultiverseApp
import com.gemini.multiverse.ui.navigation.Screen
import com.gemini.multiverse.ui.theme.JarvisBorder
import com.gemini.multiverse.ui.theme.JarvisElevated
import com.gemini.multiverse.ui.theme.JarvisPrimaryGradient
import com.gemini.multiverse.ui.theme.JarvisPurple
import com.gemini.multiverse.ui.theme.JarvisSurface
import com.gemini.multiverse.ui.theme.JarvisVoid
import com.gemini.multiverse.ui.theme.TextSnow
import com.gemini.multiverse.ui.theme.TextSubtle

/**
 * Screen 1: Intelligent Voice Assistance Dashboard (Matching Photo UI)
 */
@Composable
fun HomeScreen(navController: NavController) {
    val container = remember { GeminiMultiverseApp.instance.container }
    val settingsState by container.settingsRepository.settingsState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(JarvisVoid)
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(18.dp))

        // 1. Top Header: User Profile Avatar + "Good Morning" + Hamburger Menu
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Profile Avatar with subtle glow border
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF8B5CF6), Color(0xFFD946EF))
                            )
                        )
                        .border(1.5.dp, JarvisPurple.copy(alpha = 0.6f), CircleShape)
                        .clickable { navController.navigate(Screen.Profile.route) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "User Avatar",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Good Morning",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        color = TextSubtle
                    )
                    Text(
                        text = "Mike Wheeler",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSnow
                    )
                }
            }

            // Menu Button (Round circular button on right)
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(JarvisElevated)
                    .border(1.dp, JarvisBorder, CircleShape)
                    .clickable { navController.navigate(Screen.Settings.route) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu",
                    tint = TextSnow,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // 2. Large Headline: "Intelligent Voice Assistance"
        Text(
            text = "Intelligent Voice\nAssistance",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = TextSnow,
            lineHeight = 34.sp
        )

        Spacer(modifier = Modifier.height(22.dp))

        // 3. Bento Grid: Tall Featured Card (Voice Assistance) + Stacked Cards (Video & Image Gen)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Left Tall Card: "Voice Assistance" (Vibrant Neon Flowing Gradient)
            Card(
                modifier = Modifier
                    .weight(1.05f)
                    .fillMaxSize()
                    .clip(RoundedCornerShape(24.dp))
                    .clickable { navController.navigate(Screen.Voice.route) },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(JarvisPrimaryGradient)
                        .padding(18.dp)
                ) {
                    // Play icon inside translucent circular capsule at top left
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.25f))
                            .align(Alignment.TopStart),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play Voice Assistance",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Bottom Label
                    Text(
                        text = "Voice Assistance",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.align(Alignment.BottomStart)
                    )
                }
            }

            // Right Column: Video Generation & Image Generation
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Video Generation Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { navController.navigate(Screen.Forge.route) },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = JarvisSurface),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(JarvisBorder, Color.Transparent)))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(JarvisElevated),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Videocam,
                                contentDescription = "Video Generation",
                                tint = JarvisPurple,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Text(
                            text = "Video Generation",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSnow
                        )
                    }
                }

                // Image Generation Card (Navigates to Screen 3: Create An Image)
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { navController.navigate(Screen.CreateImage.route) },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = JarvisSurface),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(JarvisBorder, Color.Transparent)))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(JarvisElevated),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = "Image Generation",
                                tint = JarvisPurple,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Text(
                            text = "Image Generation",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSnow
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        // 4. "Quick Prompts" Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Quick Prompts",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextSnow
            )
            Text(
                text = "See All",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = JarvisPurple,
                modifier = Modifier.clickable { navController.navigate(Screen.Chat.route) }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Quick Prompt Items matching the photo
        QuickPromptItem(
            icon = Icons.Default.GraphicEq,
            text = "Abstract Smooth Gradient Backgro...",
            onPlayClick = { navController.navigate(Screen.Voice.route) }
        )

        Spacer(modifier = Modifier.height(10.dp))

        QuickPromptItem(
            icon = Icons.Default.Image,
            text = "Create A Minimal Abstract 3D Envel...",
            onPlayClick = { navController.navigate(Screen.CreateImage.route) }
        )

        Spacer(modifier = Modifier.height(10.dp))

        QuickPromptItem(
            icon = Icons.Default.Videocam,
            text = "Abstract Smooth Gradient Backgro...",
            onPlayClick = { navController.navigate(Screen.Voice.route) }
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun QuickPromptItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onPlayClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(JarvisSurface)
            .border(1.dp, JarvisBorder, RoundedCornerShape(26.dp))
            .clickable { onPlayClick() }
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon in Purple/Magenta Gradient Circle
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF8B5CF6), Color(0xFFD946EF))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = text,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = TextSnow,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Circular Play Button on the right
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF9333EA), Color(0xFFC026D3))
                    )
                )
                .clickable { onPlayClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Execute Prompt",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
