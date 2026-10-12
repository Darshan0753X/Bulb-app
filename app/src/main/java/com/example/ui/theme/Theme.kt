package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
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

@Composable
fun SmitchTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
