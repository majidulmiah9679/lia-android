package com.gemini.multiverse.ui.screens.forge

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.gemini.multiverse.feature.forge.Forge3DTemplateEngine
import com.gemini.multiverse.ui.theme.CyanBioluminescent
import com.gemini.multiverse.ui.theme.EmeraldOnline
import com.gemini.multiverse.ui.theme.IndigoBorder
import com.gemini.multiverse.ui.theme.IndigoElevated
import com.gemini.multiverse.ui.theme.IndigoSurface
import com.gemini.multiverse.ui.theme.IndigoVoid
import com.gemini.multiverse.ui.theme.MarigoldLight
import com.gemini.multiverse.ui.theme.MarigoldPrimary
import com.gemini.multiverse.ui.theme.TextDim
import com.gemini.multiverse.ui.theme.TextSnow
import com.gemini.multiverse.ui.theme.TextSubtle
import com.gemini.multiverse.ui.theme.Typography
import java.io.File

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun ForgeScreen() {
    val context = LocalContext.current
    var promptInput by remember { mutableStateOf("Futuristic Cyber Cafe website with glowing espresso core") }
    var currentTitle by remember { mutableStateOf("Cyber Cafe 3D") }
    var isCodeView by remember { mutableStateOf(false) }
    var exportStatus by remember { mutableStateOf<String?>(null) }

    var generatedHtml by remember {
        mutableStateOf(Forge3DTemplateEngine.synthesize3DWebsite(promptInput, currentTitle))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IndigoVoid)
    ) {
        // Top Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(IndigoSurface)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = "3D Website Forge", style = Typography.titleLarge, color = TextSnow, fontSize = 16.sp)
                Text(text = if (isCodeView) "HTML & Three.js Inspector" else "Live WebGL Viewport", color = CyanBioluminescent, fontSize = 12.sp)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Code / Preview Toggle
                IconButton(onClick = { isCodeView = !isCodeView }) {
                    Icon(
                        imageVector = if (isCodeView) Icons.Default.Visibility else Icons.Default.Code,
                        contentDescription = "Toggle View",
                        tint = MarigoldPrimary
                    )
                }

                // Export HTML Button
                IconButton(onClick = {
                    val file = exportHtmlToFile(context, generatedHtml, "forged_site.html")
                    exportStatus = "Exported to: ${file.name}"
                }) {
                    Icon(
                        imageVector = Icons.Default.FileDownload,
                        contentDescription = "Export Standalone HTML",
                        tint = CyanBioluminescent
                    )
                }
            }
        }

        if (exportStatus != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(EmeraldOnline.copy(alpha = 0.2f))
                    .padding(vertical = 4.dp, horizontal = 16.dp)
            ) {
                Text(text = exportStatus ?: "", color = EmeraldOnline, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Viewport Area (WebGL WebView or Source Code)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (isCodeView) {
                // Code Inspector
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(IndigoVoid)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = generatedHtml,
                        color = MarigoldLight,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            } else {
                // Live Hardware-Accelerated WebGL WebView
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        WebView(ctx).apply {
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            settings.loadWithOverviewMode = true
                            settings.useWideViewPort = true
                            settings.cacheMode = WebSettings.LOAD_NO_CACHE
                            webViewClient = WebViewClient()
                            loadDataWithBaseURL("https://threejs.org", generatedHtml, "text/html", "UTF-8", null)
                        }
                    },
                    update = { webView ->
                        webView.loadDataWithBaseURL("https://threejs.org", generatedHtml, "text/html", "UTF-8", null)
                    }
                )
            }
        }

        // Bottom Prompt Synthesizer Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(IndigoSurface)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = promptInput,
                onValueChange = { promptInput = it },
                placeholder = { Text("Lia, ek cafe / solar system ki website banao...", color = TextDim, fontSize = 13.sp) },
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextSnow,
                    unfocusedTextColor = TextSnow,
                    focusedBorderColor = MarigoldPrimary,
                    unfocusedBorderColor = IndigoBorder
                ),
                shape = RoundedCornerShape(20.dp),
                maxLines = 2
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                    if (promptInput.isNotBlank()) {
                        generatedHtml = Forge3DTemplateEngine.synthesize3DWebsite(promptInput, "Forged 3D Scene")
                        isCodeView = false
                        exportStatus = null
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MarigoldPrimary)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "Forge",
                    tint = IndigoVoid
                )
            }
        }
    }
}

private fun exportHtmlToFile(context: Context, htmlContent: String, fileName: String): File {
    val dir = context.getExternalFilesDir(null) ?: context.filesDir
    val file = File(dir, fileName)
    file.writeText(htmlContent)
    return file
}
