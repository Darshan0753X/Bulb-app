package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = Color.Black,
    primaryContainer = SurfaceElevatedDark,
    onPrimaryContainer = SoftWhite,
    secondary = NeonAmber,
    onSecondary = Color.Black,
    tertiary = NeonGreen,
    background = MidnightDark,
    onBackground = SoftWhite,
    surface = SurfaceDark,
    onSurface = SoftWhite,
    surfaceVariant = SurfaceElevatedDark,
    onSurfaceVariant = MutedGray,
    outline = BorderDark
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF0091EA),
    onPrimary = Color.White,
    primaryContainer = SurfaceElevatedLight,
    onPrimaryContainer = TextPrimaryLight,
    secondary = Color(0xFFFF8F00),
    onSecondary = Color.White,
    tertiary = Color(0xFF00C853),
    background = PorcelainLight,
    onBackground = TextPrimaryLight,
    surface = SurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = SurfaceElevatedLight,
    onSurfaceVariant = TextMutedLight,
    outline = BorderLight
)

@Composable
fun SmitchTheme(
    isDarkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (isDarkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}
