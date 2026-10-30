package com.gemini.multiverse.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LiaDarkColorScheme = darkColorScheme(
    primary = MarigoldPrimary,
    onPrimary = IndigoVoid,
    primaryContainer = IndigoElevated,
    onPrimaryContainer = MarigoldLight,
    secondary = CyanBioluminescent,
    onSecondary = IndigoVoid,
    tertiary = MarigoldGlow,
    onTertiary = IndigoVoid,
    background = IndigoVoid,
    onBackground = TextSnow,
    surface = IndigoSurface,
    onSurface = TextSnow,
    surfaceVariant = IndigoElevated,
    onSurfaceVariant = TextSubtle,
    outline = IndigoBorder,
    error = RoseError
)

@Composable
fun GeminiMultiverseTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = LiaDarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            window?.let {
                it.statusBarColor = IndigoVoid.toArgb()
                it.navigationBarColor = IndigoVoid.toArgb()
                WindowCompat.getInsetsController(it, view).apply {
                    isAppearanceLightStatusBars = false
                    isAppearanceLightNavigationBars = false
                }
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
