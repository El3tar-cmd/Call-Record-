package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = HighDensityPrimary,
            onPrimary = Color(0xFF003258),
            primaryContainer = HighDensityAccentContainer,
            onPrimaryContainer = HighDensityOnAccentContainer,
            secondary = HighDensityPrimary,
            onSecondary = Color(0xFF003258),
            secondaryContainer = HighDensityAccentContainer,
            onSecondaryContainer = HighDensityOnAccentContainer,
            background = HighDensityBg,
            onBackground = HighDensityText,
            surface = Color(0xFF28282B),
            onSurface = HighDensityText,
            surfaceVariant = HighDensityBg,
            onSurfaceVariant = HighDensitySubText,
            outline = HighDensityBorder
        )
    } else {
        lightColorScheme(
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
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
