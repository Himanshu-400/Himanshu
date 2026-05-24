package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class StudyNovaThemeStyle {
    COSMIC_DARK,
    AMOLED,
    BLUE_PREMIUM,
    PURPLE_FUTURISTIC,
    CYBER_YELLOW,
    NEON_GREEN,
    CRIMSON_RED,
    SOLAR_ECLIPSE,
    QUANTUM_SILVER,
    HOLOGRAPHIC_TEAL,
    VIRTUAL_VIOLET,
    MIDNIGHT_GOLD,
    TOXIC_PUNK,
    GALAXY_ORBIT
}

// 1. COSMIC_DARK (Default premium brand theme)
private val CosmicDarkColorScheme = darkColorScheme(
    primary = CyberPurple,
    onPrimary = Color.White,
    secondary = ElectricBlue,
    onSecondary = Color.White,
    tertiary = NeonCyan,
    background = DeepSpaceBlue,
    onBackground = Color(0xFFE2E4F0),
    surface = Color(0xFF0F1223),
    onSurface = Color(0xFFE2E4F0),
    surfaceVariant = Color(0x33864AF9),
    onSurfaceVariant = Color(0xFF864AF9)
)

// 2. AMOLED (Pure blacks for OLED efficiency)
private val AmoledColorScheme = darkColorScheme(
    primary = CyberPurple,
    onPrimary = Color.White,
    secondary = CyberPink,
    onSecondary = Color.White,
    tertiary = NeonPurpleAccent,
    background = AmoledBlack,
    onBackground = Color(0xFFF0F0FF),
    surface = Color(0xFF0C0C0C),
    onSurface = Color(0xFFF0F0FF),
    surfaceVariant = Color(0xFF1E1E1E),
    onSurfaceVariant = Color(0xFFFF52A2)
)

// 3. BLUE_PREMIUM (Modern cyan and electric electric)
private val BluePremiumColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = Color(0xFF040610),
    secondary = ElectricBlue,
    onSecondary = Color.White,
    tertiary = CyberPurple,
    background = SoftDarkBlue,
    onBackground = Color(0xFFEAF4FF),
    surface = Color(0xFF0D1537),
    onSurface = Color(0xFFEAF4FF),
    surfaceVariant = Color(0x3300F0FF),
    onSurfaceVariant = Color(0xFF00F0FF)
)

// 4. PURPLE_FUTURISTIC (Neon cyber pinks and high violet glows)
private val PurpleFuturisticColorScheme = darkColorScheme(
    primary = CyberPurple,
    onPrimary = Color.White,
    secondary = CyberPink,
    onSecondary = Color.White,
    tertiary = NeonPurpleAccent,
    background = DeepSpaceBlue,
    onBackground = Color(0xFFF6EEFF),
    surface = Color(0xFF13112E),
    onSurface = Color(0xFFF6EEFF),
    surfaceVariant = Color(0x33D300C5),
    onSurfaceVariant = Color(0xFFD300C5)
)

// 5. CYBER_YELLOW (Neon yellow and dark gray)
private val CyberYellowColorScheme = darkColorScheme(
    primary = Color(0xFFE5FF00),
    onPrimary = Color.Black,
    secondary = Color(0xFFFFA200),
    onSecondary = Color.Black,
    tertiary = Color(0xFF00FFCC),
    background = Color(0xFF0D0D0D),
    onBackground = Color.White,
    surface = Color(0xFF1A1A1A),
    onSurface = Color.White,
    surfaceVariant = Color(0x33E5FF00),
    onSurfaceVariant = Color(0xFFE5FF00)
)

// 6. NEON_GREEN (Matrix style)
private val NeonGreenColorScheme = darkColorScheme(
    primary = Color(0xFF00FF41),
    onPrimary = Color.Black,
    secondary = Color(0xFF008F11),
    onSecondary = Color.Black,
    tertiary = Color(0xFF003B00),
    background = Color.Black,
    onBackground = Color(0xFF00FF41),
    surface = Color(0xFF05170A),
    onSurface = Color(0xFF00FF41),
    surfaceVariant = Color(0x3300FF41),
    onSurfaceVariant = Color(0xFF00FF41)
)

// 7. CRIMSON_RED (Dark red tech)
private val CrimsonRedColorScheme = darkColorScheme(
    primary = Color(0xFFFF2A2A),
    onPrimary = Color.White,
    secondary = Color(0xFF8B0000),
    onSecondary = Color.White,
    tertiary = Color(0xFFFF52A2),
    background = Color(0xFF0C0202),
    onBackground = Color.White,
    surface = Color(0xFF190404),
    onSurface = Color.White,
    surfaceVariant = Color(0x33FF2A2A),
    onSurfaceVariant = Color(0xFFFF2A2A)
)

// 8. SOLAR_ECLIPSE (Orange and deep teal)
private val SolarEclipseColorScheme = darkColorScheme(
    primary = Color(0xFFFF7A00),
    onPrimary = Color.White,
    secondary = Color(0xFFFFD100),
    onSecondary = Color.Black,
    tertiary = Color(0xFF00E5FF),
    background = Color(0xFF051014),
    onBackground = Color.White,
    surface = Color(0xFF0D222A),
    onSurface = Color.White,
    surfaceVariant = Color(0x33FF7A00),
    onSurfaceVariant = Color(0xFFFF7A00)
)

// 9. QUANTUM_SILVER (Clean silvers and glowing white)
private val QuantumSilverColorScheme = darkColorScheme(
    primary = Color(0xFFE0E0E0),
    onPrimary = Color.Black,
    secondary = Color(0xFFA0A0A0),
    onSecondary = Color.Black,
    tertiary = Color(0xFFFFFFFF),
    background = Color(0xFF121212),
    onBackground = Color(0xFFE0E0E0),
    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFE0E0E0),
    surfaceVariant = Color(0x33E0E0E0),
    onSurfaceVariant = Color(0xFFFFFFFF)
)

// 10. HOLOGRAPHIC_TEAL
private val HolographicTealColorScheme = darkColorScheme(
    primary = Color(0xFF00F0FF),
    onPrimary = Color.Black,
    secondary = Color(0xFF00FFA3),
    onSecondary = Color.Black,
    tertiary = Color(0xFF7000FF),
    background = Color(0xFF00111A),
    onBackground = Color.White,
    surface = Color(0xFF002233),
    onSurface = Color.White,
    surfaceVariant = Color(0x3300F0FF),
    onSurfaceVariant = Color(0xFF00F0FF)
)

// 11. VIRTUAL_VIOLET
private val VirtualVioletColorScheme = darkColorScheme(
    primary = Color(0xFFA200FF),
    onPrimary = Color.White,
    secondary = Color(0xFF6A00FF),
    onSecondary = Color.White,
    tertiary = Color(0xFFFF00D4),
    background = Color(0xFF0A0014),
    onBackground = Color.White,
    surface = Color(0xFF140028),
    onSurface = Color.White,
    surfaceVariant = Color(0x33A200FF),
    onSurfaceVariant = Color(0xFFA200FF)
)

// 12. MIDNIGHT_GOLD
private val MidnightGoldColorScheme = darkColorScheme(
    primary = Color(0xFFFFD700),
    onPrimary = Color.Black,
    secondary = Color(0xFFFFA500),
    onSecondary = Color.Black,
    tertiary = Color(0xFFFFFFFF),
    background = Color(0xFF000814),
    onBackground = Color.White,
    surface = Color(0xFF001D3D),
    onSurface = Color.White,
    surfaceVariant = Color(0x33FFD700),
    onSurfaceVariant = Color(0xFFFFD700)
)

// 13. TOXIC_PUNK
private val ToxicPunkColorScheme = darkColorScheme(
    primary = Color(0xFFCCFF00),
    onPrimary = Color.Black,
    secondary = Color(0xFFFF003C),
    onSecondary = Color.White,
    tertiary = Color(0xFF00F0FF),
    background = Color(0xFF101010),
    onBackground = Color.White,
    surface = Color(0xFF1C1C1C),
    onSurface = Color.White,
    surfaceVariant = Color(0x33CCFF00),
    onSurfaceVariant = Color(0xFFCCFF00)
)

// 14. GALAXY_ORBIT
private val GalaxyOrbitColorScheme = darkColorScheme(
    primary = Color(0xFFD300C5),
    onPrimary = Color.White,
    secondary = Color(0xFF1F51FF),
    onSecondary = Color.White,
    tertiary = Color(0xFFFFD700),
    background = Color(0xFF050014),
    onBackground = Color.White,
    surface = Color(0xFF0F0028),
    onSurface = Color.White,
    surfaceVariant = Color(0x33D300C5),
    onSurfaceVariant = Color(0xFFD300C5)
)

// Standard Light scheme (as graceful fallback)
private val LightColorScheme = lightColorScheme(
    primary = PrimaryLight,
    onPrimary = Color.White,
    secondary = SecondaryLight,
    onSecondary = Color.White,
    background = BackgroundLight,
    onBackground = Color(0xFF10141C),
    surface = SurfaceLight,
    onSurface = Color(0xFF10141C)
)

@Composable
fun StudyNovaTheme(
    themeStyle: StudyNovaThemeStyle = StudyNovaThemeStyle.COSMIC_DARK,
    isDarkMenu: Boolean = true, // We focus on the glorious high-end deep-gradient visual themes
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        !isDarkMenu -> LightColorScheme
        themeStyle == StudyNovaThemeStyle.COSMIC_DARK -> CosmicDarkColorScheme
        themeStyle == StudyNovaThemeStyle.AMOLED -> AmoledColorScheme
        themeStyle == StudyNovaThemeStyle.BLUE_PREMIUM -> BluePremiumColorScheme
        themeStyle == StudyNovaThemeStyle.PURPLE_FUTURISTIC -> PurpleFuturisticColorScheme
        themeStyle == StudyNovaThemeStyle.CYBER_YELLOW -> CyberYellowColorScheme
        themeStyle == StudyNovaThemeStyle.NEON_GREEN -> NeonGreenColorScheme
        themeStyle == StudyNovaThemeStyle.CRIMSON_RED -> CrimsonRedColorScheme
        themeStyle == StudyNovaThemeStyle.SOLAR_ECLIPSE -> SolarEclipseColorScheme
        themeStyle == StudyNovaThemeStyle.QUANTUM_SILVER -> QuantumSilverColorScheme
        themeStyle == StudyNovaThemeStyle.HOLOGRAPHIC_TEAL -> HolographicTealColorScheme
        themeStyle == StudyNovaThemeStyle.VIRTUAL_VIOLET -> VirtualVioletColorScheme
        themeStyle == StudyNovaThemeStyle.MIDNIGHT_GOLD -> MidnightGoldColorScheme
        themeStyle == StudyNovaThemeStyle.TOXIC_PUNK -> ToxicPunkColorScheme
        themeStyle == StudyNovaThemeStyle.GALAXY_ORBIT -> GalaxyOrbitColorScheme
        else -> CosmicDarkColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
