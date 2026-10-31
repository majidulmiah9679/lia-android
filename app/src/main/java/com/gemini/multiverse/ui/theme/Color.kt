package com.gemini.multiverse.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ==========================================
// JARVIS FUTURISTIC PURPLE THEME (Matching Photo)
// ==========================================
val JarvisVoid = Color(0xFF07040D)          // Deep cosmic black-purple
val JarvisSurface = Color(0xFF140D24)       // Dark slate purple cards
val JarvisElevated = Color(0xFF1D1433)      // Elevated pill containers & inputs
val JarvisBorder = Color(0x33A855F7)        // Subtle purple glow borders
val JarvisGlass = Color(0x80140D24)

// Vibrant Glow & Energy Accents
val JarvisPurple = Color(0xFFA855F7)        // Electric violet
val JarvisMagenta = Color(0xFFD946EF)       // Glowing magenta
val JarvisPink = Color(0xFFF472B6)          // Soft pink haze
val JarvisElectricBlue = Color(0xFF38BDF8)  // Neon electric blue
val JarvisCyan = Color(0xFF22D3EE)          // Cyan highlight

// Gradients
val JarvisPrimaryGradient = Brush.verticalGradient(
    listOf(Color(0xFF9333EA), Color(0xFFC026D3), Color(0xFF7C3AED))
)
val JarvisHorizontalGradient = Brush.horizontalGradient(
    listOf(Color(0xFFA855F7), Color(0xFFD946EF))
)
val JarvisOrbFlowGradient = Brush.linearGradient(
    listOf(Color(0xFF7C3AED), Color(0xFFC026D3), Color(0xFF38BDF8), Color(0xFFF472B6))
)

// Legacy Indigo / Dark Palette Compatibility
val IndigoVoid = JarvisVoid
val IndigoSurface = JarvisSurface
val IndigoElevated = JarvisElevated
val IndigoBorder = JarvisBorder
val IndigoGlass = JarvisGlass

val VoidBlack = JarvisVoid
val DarkSurface = JarvisSurface
val GlassSurface = JarvisElevated
val GlassBorder = JarvisBorder

// Marigold Accents (Sun & Golden Glow)
val MarigoldPrimary = Color(0xFFA855F7) // Remapped to Jarvis Purple as primary
val MarigoldLight = Color(0xFFD946EF)
val MarigoldGlow = Color(0xFFC026D3)
val MarigoldDeep = Color(0xFF7C3AED)

// Bioluminescent & State Accents
val CyanBioluminescent = JarvisElectricBlue
val EmeraldOnline = Color(0xFF10B981)
val RoseError = Color(0xFFF43F5E)

// Text Colors
val TextSnow = Color(0xFFF8FAFC)
val TextSubtle = Color(0xFF94A3B8)
val TextDim = Color(0xFF64748B)

val TextPrimary = TextSnow
val TextSecondary = TextSubtle
val TextMuted = TextDim

// Neon Aliases
val NeonCyan = JarvisElectricBlue
val NeonPurple = Color(0xFFA855F7)
val NeonMagenta = Color(0xFFD946EF)
val NeonGreen = EmeraldOnline
val NeonAmber = Color(0xFFFF9E00)

// Persona Aura Glows
val AuraNormal = Color(0xFFC026D3)
val AuraFriendly = Color(0xFFA855F7)
val AuraFunny = Color(0xFFFF007F)
val AuraTeacher = Color(0xFF38BDF8)
val AuraDeveloper = Color(0xFF10B981)
val AuraNautanki = Color(0xFFFF1493)
val AuraPhilosopher = Color(0xFF8B5CF6)
val AuraCoach = Color(0xFFEF4444)
val AuraPoet = Color(0xFFD97706)
val AuraExecutive = Color(0xFF64748B)
