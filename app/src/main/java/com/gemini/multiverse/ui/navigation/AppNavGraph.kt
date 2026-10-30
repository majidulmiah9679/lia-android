package com.gemini.multiverse.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.gemini.multiverse.ui.screens.about.AboutScreen
import com.gemini.multiverse.ui.screens.chat.ChatScreen
import com.gemini.multiverse.ui.screens.debug.DebugScreen
import com.gemini.multiverse.ui.screens.forge.ForgeScreen
import com.gemini.multiverse.ui.screens.history.HistoryScreen
import com.gemini.multiverse.ui.screens.home.HomeScreen
import com.gemini.multiverse.ui.screens.orb.OrbScreen
import com.gemini.multiverse.ui.screens.orbstyle.OrbStyleScreen
import com.gemini.multiverse.ui.screens.permissions.PermissionsScreen
import com.gemini.multiverse.ui.screens.personalities.PersonalitiesScreen
import com.gemini.multiverse.ui.screens.privacy.PrivacyScreen
import com.gemini.multiverse.ui.screens.profile.ProfileScreen
import com.gemini.multiverse.ui.screens.settings.SettingsScreen
import com.gemini.multiverse.ui.theme.DarkSurface
import com.gemini.multiverse.ui.theme.MarigoldPrimary
import com.gemini.multiverse.ui.theme.TextMuted
import com.gemini.multiverse.ui.theme.VoidBlack

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            // Only show bottom navigation on primary root tabs
            val isPrimaryTab = Screen.bottomNavItems.any { it.route == currentRoute }
            if (isPrimaryTab || currentRoute == null) {
                NavigationBar(
                    containerColor = DarkSurface,
                    tonalElevation = 8.dp
                ) {
                    Screen.bottomNavItems.forEach { screen ->
                        val isSelected = currentRoute == screen.route
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = screen.icon,
                                    contentDescription = screen.title
                                )
                            },
                            label = {
                                Text(text = screen.title)
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = VoidBlack,
                                selectedTextColor = MarigoldPrimary,
                                indicatorColor = MarigoldPrimary,
                                unselectedIconColor = TextMuted,
                                unselectedTextColor = TextMuted
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier
                .fillMaxSize()
                .background(VoidBlack)
                .padding(innerPadding)
        ) {
            // Primary Screens
            composable(Screen.Home.route) {
                HomeScreen(navController)
            }
            composable(Screen.Voice.route) {
                OrbScreen()
            }
            composable(Screen.Chat.route) {
                ChatScreen()
            }
            composable(Screen.Forge.route) {
                ForgeScreen()
            }
            composable(Screen.Settings.route) {
                SettingsScreen()
            }

            // Auxiliary & System Screens
            composable(Screen.History.route) {
                HistoryScreen()
            }
            composable(Screen.Personalities.route) {
                PersonalitiesScreen()
            }
            composable(Screen.OrbStyle.route) {
                OrbStyleScreen()
            }
            composable(Screen.Permissions.route) {
                PermissionsScreen()
            }
            composable(Screen.Profile.route) {
                ProfileScreen()
            }
            composable(Screen.Privacy.route) {
                PrivacyScreen()
            }
            composable(Screen.About.route) {
                AboutScreen()
            }
            composable(Screen.Debug.route) {
                DebugScreen()
            }
        }
    }
}
