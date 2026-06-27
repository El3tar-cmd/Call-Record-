package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// High Density Theme Color Palette
val HighDensityBg: Color
    @Composable get() = if (isSystemInDarkTheme()) Color(0xFF141419) else Color(0xFFF7F9FF)

val HighDensityText: Color
    @Composable get() = if (isSystemInDarkTheme()) Color(0xFFE2E2E6) else Color(0xFF1B1B1F)

val HighDensityPrimary: Color
    @Composable get() = if (isSystemInDarkTheme()) Color(0xFF80BFFF) else Color(0xFF0061A4)

val HighDensityOnPrimary: Color
    @Composable get() = if (isSystemInDarkTheme()) Color(0xFF003258) else Color.White

val HighDensityAccentContainer: Color
    @Composable get() = if (isSystemInDarkTheme()) Color(0xFF00497D) else Color(0xFFD1E4FF)

val HighDensityOnAccentContainer: Color
    @Composable get() = if (isSystemInDarkTheme()) Color(0xFFD1E4FF) else Color(0xFF001D36)

val HighDensityBorder: Color
    @Composable get() = if (isSystemInDarkTheme()) Color(0xFF44474E) else Color(0xFFDDE2F0)

val HighDensitySubText: Color
    @Composable get() = if (isSystemInDarkTheme()) Color(0xFFAAAAB4) else Color(0xFF44474E)

val HighDensityCardBg: Color
    @Composable get() = if (isSystemInDarkTheme()) Color(0xFF1E1E22) else Color.White

val SoftGray = Color(0xFF8E8E93)
val WhiteIce = Color(0xFFF5F5F7)

val InboundCallColor = Color(0xFF29B6F6)
val OutboundCallColor = Color(0xFF66BB6A)
val MicRecordingColor = Color(0xFFFF7043)

val WhatsAppColor = Color(0xFF25D366)
val MessengerColor = Color(0xFF0084FF)
val CellularColor = Color(0xFF90A4AE)
