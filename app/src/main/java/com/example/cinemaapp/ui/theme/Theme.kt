package com.example.cinemaapp.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val VelvetRougeColorScheme = darkColorScheme(
    primary          = Color(0xFFFF4D6D),
    onPrimary        = Color(0xFF2D0011),
    primaryContainer = Color(0xFF7A0026),
    onPrimaryContainer = Color(0xFFFFD9E2),
    secondary        = Color(0xFFFFB3C1),
    onSecondary      = Color(0xFF3D0017),
    secondaryContainer = Color(0xFF5C0024),
    onSecondaryContainer = Color(0xFFFFD9E2),
    tertiary         = Color(0xFFFFCC80),
    onTertiary       = Color(0xFF3D2100),
    tertiaryContainer = Color(0xFF5C3200),
    onTertiaryContainer = Color(0xFFFFE0B2),
    background       = Color(0xFF100C0D),
    onBackground     = Color(0xFFEDE0E2),
    surface          = Color(0xFF1A1315),
    onSurface        = Color(0xFFEDE0E2),
    surfaceVariant   = Color(0xFF261C1F),
    onSurfaceVariant = Color(0xFFD5C5C8),
    outline          = Color(0xFF5C3F44),
    inverseSurface   = Color(0xFFEDE0E2),
    inverseOnSurface = Color(0xFF1A1315),
)

@Composable
fun CinemaAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = VelvetRougeColorScheme,
        content = content
    )
}