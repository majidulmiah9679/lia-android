package com.gemini.multiverse.ui.screens.settings

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gemini.multiverse.BuildConfig
import com.gemini.multiverse.GeminiMultiverseApp
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

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = remember {
        SettingsViewModel(GeminiMultiverseApp.instance.container.settingsRepository)
    }
) {
    val uiState by viewModel.uiState.collectAsState()
    var inputApiKey by remember(uiState.apiKey) { mutableStateOf(uiState.apiKey) }
    var isApiKeyVisible by remember { mutableStateOf(false) }

    var inputLicenseKey by remember(uiState.licenseKey) { mutableStateOf(uiState.licenseKey) }
    var isLicenseVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IndigoVoid)
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Title Header
        Text(
            text = "Lia Settings",
            style = Typography.displayLarge,
            color = TextSnow
        )
        Text(
            text = "Configure API keys, Voice Engine & System Preferences",
            style = Typography.bodyMedium,
            color = TextSubtle
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 1. Gemini API Key Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, IndigoBorder, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = IndigoSurface)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = null,
                            tint = MarigoldPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Gemini API Key",
                            style = Typography.titleLarge,
                            color = TextSnow
                        )
                    }

                    if (uiState.hasApiKey) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = EmeraldOnline,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Active",
                                color = EmeraldOnline,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Requires a Google Gemini API key with Gemini Multimodal Live API support.",
                    style = Typography.bodyMedium,
                    color = TextSubtle
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = inputApiKey,
                    onValueChange = { inputApiKey = it },
                    label = { Text("Paste AIzaSy... Key", color = TextDim) },
                    visualTransformation = if (isApiKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { isApiKeyVisible = !isApiKeyVisible }) {
                            Icon(
                                imageVector = if (isApiKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Toggle key visibility",
                                tint = TextSubtle
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextSnow,
                        unfocusedTextColor = TextSnow,
                        focusedBorderColor = MarigoldPrimary,
                        unfocusedBorderColor = IndigoBorder,
                        focusedLabelColor = MarigoldPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { viewModel.onApiKeySave(inputApiKey) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MarigoldPrimary,
                            contentColor = IndigoVoid
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Save Key", fontWeight = FontWeight.Bold)
                    }

                    if (uiState.hasApiKey) {
                        Button(
                            onClick = {
                                inputApiKey = ""
                                viewModel.onApiKeyClear()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = IndigoElevated,
                                contentColor = RoseError
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Clear")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 2. Direct Flavor: Access Key Licensing Card (Part 13 Integration)
        if (BuildConfig.ENABLE_LICENSING) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, IndigoBorder, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = IndigoSurface)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = CyanBioluminescent,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Direct Access Licensing",
                                style = Typography.titleLarge,
                                color = TextSnow
                            )
                        }

                        val statusColor = if (uiState.isLicenseValid) EmeraldOnline else RoseError
                        val statusText = if (uiState.isLicenseValid) "Verified" else "Locked"
                        Text(
                            text = statusText,
                            color = statusColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Enter your direct-tier license key (Format: LIA-XXXX-XXXX-XXXX) to unlock the Visual Agent & Direct Tools.",
                        style = Typography.bodyMedium,
                        color = TextSubtle
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = inputLicenseKey,
                        onValueChange = { inputLicenseKey = it },
                        label = { Text("LIA-XXXX-XXXX-XXXX", color = TextDim) },
                        visualTransformation = if (isLicenseVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { isLicenseVisible = !isLicenseVisible }) {
                                Icon(
                                    imageVector = if (isLicenseVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle license visibility",
                                    tint = TextSubtle
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextSnow,
                            unfocusedTextColor = TextSnow,
                            focusedBorderColor = CyanBioluminescent,
                            unfocusedBorderColor = IndigoBorder,
                            focusedLabelColor = CyanBioluminescent
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { viewModel.onLicenseKeySave(inputLicenseKey) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyanBioluminescent,
                            contentColor = IndigoVoid
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Validate License", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
        }

        // 3. Language & Polyglot Setting
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, IndigoBorder, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = IndigoSurface)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.RecordVoiceOver,
                        contentDescription = null,
                        tint = MarigoldGlow,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Primary Language Mode",
                        style = Typography.titleLarge,
                        color = TextSnow
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                val languages = listOf(
                    "hinglish" to "Hinglish (हिंदी + English - Recommended)",
                    "hindi" to "Shuddh Hindi (शुद्ध हिंदी)",
                    "english" to "Global English"
                )

                languages.forEach { (code, label) ->
                    val isSelected = uiState.selectedLanguage == code
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) IndigoElevated else Color.Transparent)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) MarigoldPrimary else Color.Transparent,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { viewModel.onLanguageSelect(code) }
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) MarigoldPrimary else TextSubtle,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 4. Audio Engine & Background Service
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, IndigoBorder, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = IndigoSurface)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = null,
                        tint = CyanBioluminescent,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Voice Pipeline & Echo Guard",
                        style = Typography.titleLarge,
                        color = TextSnow
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Echo Guard Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Acoustic Echo Guard (AEC)",
                            style = Typography.bodyLarge,
                            color = TextSnow
                        )
                        Text(
                            text = "Suppresses speaker feedback during live full-duplex talk",
                            style = Typography.bodyMedium,
                            color = TextSubtle
                        )
                    }
                    Switch(
                        checked = uiState.isEchoCancellationEnabled,
                        onCheckedChange = { viewModel.onEchoCancellationToggle(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = IndigoVoid,
                            checkedTrackColor = MarigoldPrimary,
                            uncheckedThumbColor = TextSubtle,
                            uncheckedTrackColor = IndigoElevated
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Background Service Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Background Voice Session",
                            style = Typography.bodyLarge,
                            color = TextSnow
                        )
                        Text(
                            text = "Keep talking to Lia when screen is off or in other apps",
                            style = Typography.bodyMedium,
                            color = TextSubtle
                        )
                    }
                    Switch(
                        checked = uiState.isBackgroundServiceEnabled,
                        onCheckedChange = { viewModel.onBackgroundServiceToggle(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = IndigoVoid,
                            checkedTrackColor = MarigoldPrimary,
                            uncheckedThumbColor = TextSubtle,
                            uncheckedTrackColor = IndigoElevated
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
