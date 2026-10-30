package com.gemini.multiverse.ui.screens.permissions

import android.Manifest
import android.content.pm.PackageManager
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.gemini.multiverse.ui.theme.EmeraldOnline
import com.gemini.multiverse.ui.theme.IndigoBorder
import com.gemini.multiverse.ui.theme.IndigoSurface
import com.gemini.multiverse.ui.theme.IndigoVoid
import com.gemini.multiverse.ui.theme.MarigoldPrimary
import com.gemini.multiverse.ui.theme.RoseError
import com.gemini.multiverse.ui.theme.TextSnow
import com.gemini.multiverse.ui.theme.TextSubtle
import com.gemini.multiverse.ui.theme.Typography

@Composable
fun PermissionsScreen() {
    val context = LocalContext.current

    val permissionsList = listOf(
        PermissionItem(
            name = "Microphone (Audio Capture)",
            description = "Essential for Gemini Live real-time full-duplex voice talk.",
            manifestPermission = Manifest.permission.RECORD_AUDIO,
            icon = Icons.Default.Mic
        ),
        PermissionItem(
            name = "Camera (Flashlight / Torch)",
            description = "Used to toggle the torch when you say 'Flashlight on karo'.",
            manifestPermission = Manifest.permission.CAMERA,
            icon = Icons.Default.Camera
        ),
        PermissionItem(
            name = "Phone & Contacts",
            description = "Resolves contact names for 'Mummy ko call karo' commands.",
            manifestPermission = Manifest.permission.CALL_PHONE,
            icon = Icons.Default.Contacts
        ),
        PermissionItem(
            name = "Notifications",
            description = "Shows live voice status controls in notification shade.",
            manifestPermission = Manifest.permission.POST_NOTIFICATIONS,
            icon = Icons.Default.Notifications
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IndigoVoid)
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Text(text = "App Permissions", style = Typography.displayLarge, color = TextSnow)
        Text(text = "Hardware and system permission status for Lia's abilities", style = Typography.bodyMedium, color = TextSubtle)

        Spacer(modifier = Modifier.height(20.dp))

        permissionsList.forEach { item ->
            val isGranted = ContextCompat.checkSelfPermission(context, item.manifestPermission) == PackageManager.PERMISSION_GRANTED

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .border(1.dp, IndigoBorder, RoundedCornerShape(14.dp)),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = IndigoSurface)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = item.icon, contentDescription = null, tint = MarigoldPrimary, modifier = Modifier.size(26.dp))
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(text = item.name, style = Typography.titleLarge, color = TextSnow, fontSize = 15.sp)
                            Text(text = item.description, color = TextSubtle, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Icon(
                        imageVector = if (isGranted) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (isGranted) EmeraldOnline else RoseError,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

data class PermissionItem(
    val name: String,
    val description: String,
    val manifestPermission: String,
    val icon: ImageVector
)
