package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val HighDensityColorScheme = lightColorScheme(
    primary = HighDensityPrimary,
    onPrimary = Color.White,
    primaryContainer = HighDensityAccentContainer,
    onPrimaryContainer = HighDensityOnAccentContainer,
    secondary = HighDensityPrimary,
    onSecondary = Color.White,
    secondaryContainer = HighDensityAccentContainer,
    onSecondaryContainer = HighDensityOnAccentContainer,
    background = HighDensityBg,
    onBackground = HighDensityText,
    surface = Color.White,
    onSurface = HighDensityText,
    surfaceVariant = HighDensityBg,
    onSurfaceVariant = HighDensitySubText,
    outline = HighDensityBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false, // High Density theme is light and clean
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = HighDensityColorScheme,
        typography = Typography,
        content = content
    )
}
