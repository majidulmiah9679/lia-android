package com.gemini.multiverse.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Complete list of 12 Screens for Lia.
 */
sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Home : Screen("home", "Home", Icons.Default.Home)
    data object Voice : Screen("voice", "Voice", Icons.Default.Mic)
    data object CreateImage : Screen("create_image", "Create An Image", Icons.Default.ColorLens)
    data object Chat : Screen("chat", "Chat", Icons.Default.Chat)
    data object Forge : Screen("forge", "3D Forge", Icons.Default.Language)
    data object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    data object History : Screen("history", "History", Icons.Default.History)
    data object Personalities : Screen("personalities", "Personas", Icons.Default.Person)
    data object OrbStyle : Screen("orb_style", "Orb Style", Icons.Default.ColorLens)
    data object Permissions : Screen("permissions", "Permissions", Icons.Default.CheckCircle)
    data object Profile : Screen("profile", "Profile", Icons.Default.Person)
    data object Privacy : Screen("privacy", "Privacy", Icons.Default.Security)
    data object About : Screen("about", "About", Icons.Default.Info)
    data object Debug : Screen("debug", "Debug", Icons.Default.BugReport)

    companion object {
        // Primary Bottom Bar Screens
        val bottomNavItems: List<Screen> = listOf(Home, Voice, Chat, Forge, Settings)
    }
}
