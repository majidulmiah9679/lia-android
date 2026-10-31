package com.gemini.multiverse.ui.screens.image

import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.gemini.multiverse.ui.navigation.Screen
import com.gemini.multiverse.ui.theme.JarvisBorder
import com.gemini.multiverse.ui.theme.JarvisElectricBlue
import com.gemini.multiverse.ui.theme.JarvisElevated
import com.gemini.multiverse.ui.theme.JarvisPink
import com.gemini.multiverse.ui.theme.JarvisPrimaryGradient
import com.gemini.multiverse.ui.theme.JarvisPurple
import com.gemini.multiverse.ui.theme.JarvisSurface
import com.gemini.multiverse.ui.theme.JarvisVoid
import com.gemini.multiverse.ui.theme.TextSnow
import com.gemini.multiverse.ui.theme.TextSubtle

/**
 * Screen 3: "Create An Image" Tool Generation Screen (Matching Photo UI)
 * Features:
 * - Top Bar with back button and settings
 * - Exact Prompt Card from photo
 * - "Image Created" Card with flowing abstract gradient artwork & download button
 * - "Ask Anything" input field with Send button
 */
@Composable
fun CreateImageScreen(navController: NavController) {
    var promptInput by remember {
        mutableStateOf(
            "Abstract Smooth Gradient Background With Flowing Organic Shapes, Blending Deep Purple, Violet, Electric Blue, And Soft Pink Haze. A Modern, Futuristic, And Minimal Style, Perfect For Tech And Creative Designs."
        )
    }

    var askAnythingText by remember { mutableStateOf("") }
    var isGenerating by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(JarvisVoid)
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(18.dp))

        // 1. Top Bar: Back Button + Title + Settings Tune Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(JarvisSurface)
                    .border(1.dp, JarvisBorder, CircleShape)
                    .clickable { navController.popBackStack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextSnow,
                    modifier = Modifier.size(20.dp)
                )
            }

            Text(
                text = "Create An Image",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextSnow
            )

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(JarvisSurface)
                    .border(1.dp, JarvisBorder, CircleShape)
                    .clickable { navController.navigate(Screen.Settings.route) },
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

        // 2. Prompt Card showing prompt text
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp)),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = JarvisSurface),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(JarvisBorder, Color.Transparent)))
        ) {
            Box(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = promptInput,
                    fontSize = 12.5.sp,
                    color = Color(0xFFE2E8F0),
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.Normal
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 3. Section Title: "Image Created"
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            Text(
                text = "Image Created",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextSnow
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 4. Generated Image Artwork Card with Flowing Gradient
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp)),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = JarvisSurface),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(JarvisBorder, Color.Transparent)))
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Generative Abstract Canvas Art (Flowing Violet, Electric Blue, Pink Haze)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp)
                        .clip(RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height

                        // Dark Purple / Blue gradient base
                        drawRect(
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFF0F0721), Color(0xFF2E1065), Color(0xFF1E1B4B)),
                                start = Offset(0f, 0f),
                                end = Offset(w, h)
                            )
                        )

                        // Flowing Wave 1 (Deep Violet & Magenta)
                        val wavePath1 = Path().apply {
                            moveTo(0f, h * 0.45f)
                            cubicTo(w * 0.25f, h * 0.15f, w * 0.70f, h * 0.75f, w, h * 0.35f)
                            lineTo(w, h)
                            lineTo(0f, h)
                            close()
                        }
                        drawPath(
                            path = wavePath1,
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFF9333EA), Color(0xFFC026D3), Color(0xFF4F46E5)),
                                start = Offset(0f, h * 0.3f),
                                end = Offset(w, h)
                            )
                        )

                        // Flowing Wave 2 (Electric Blue & Soft Pink Haze)
                        val wavePath2 = Path().apply {
                            moveTo(0f, h * 0.65f)
                            cubicTo(w * 0.40f, h * 0.35f, w * 0.65f, h * 0.90f, w, h * 0.55f)
                            lineTo(w, h)
                            lineTo(0f, h)
                            close()
                        }
                        drawPath(
                            path = wavePath2,
                            brush = Brush.linearGradient(
                                colors = listOf(JarvisElectricBlue.copy(alpha = 0.85f), JarvisPink.copy(alpha = 0.75f)),
                                start = Offset(0f, h * 0.5f),
                                end = Offset(w, h)
                            )
                        )
                    }
                }

                // Info Footer Bar inside Card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(JarvisElevated),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = null,
                                tint = JarvisPurple,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "Abstract Smooth Gradient",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextSnow
                            )
                            Text(
                                text = "Ready  ·  17 Sep, 2026",
                                fontSize = 12.sp,
                                color = TextSubtle
                            )
                        }
                    }

                    // Download Button (Pink / Purple Circle with Download Arrow)
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFFC026D3), Color(0xFFE879F9))
                                )
                            )
                            .clickable { /* Download image */ },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowDownward,
                            contentDescription = "Download Image",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // 5. Bottom Input Bar: "Ask Anything" Capsule Field + Purple Send Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(JarvisSurface)
                .border(1.dp, JarvisBorder, RoundedCornerShape(28.dp))
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextField(
                value = askAnythingText,
                onValueChange = { askAnythingText = it },
                placeholder = {
                    Text(
                        text = "Ask Anything",
                        color = TextSubtle,
                        fontSize = 14.sp
                    )
                },
                modifier = Modifier.weight(1f),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedTextColor = TextSnow,
                    unfocusedTextColor = TextSnow,
                    cursorColor = JarvisPurple,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                singleLine = true
            )

            // Send Button with Diagonal Paper Plane Icon
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(JarvisPrimaryGradient)
                    .clickable {
                        if (askAnythingText.isNotBlank()) {
                            promptInput = askAnythingText
                            askAnythingText = ""
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}
