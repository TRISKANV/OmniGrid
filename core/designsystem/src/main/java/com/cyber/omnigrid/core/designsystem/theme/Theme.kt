package com.cyber.omnigrid.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Paleta Monocromática Premium con Acento Tecnológico
val TrueBlack = Color(0xFF000000)
val DarkGrayCard = Color(0xFF0A0A0A)
val BorderGray = Color(0xFF1F1F1F)
val TextPrimary = Color(0xFFF5F5F5)
val TextSecondary = Color(0xFF8E8E93)

// El único acento de color (Estilo Flipper Orange o Cyber Mint)
val CyberAccent = Color(0xFFFF6B00) 
val CyberAccentGlow = Color(0x1AFF6B00)

private val CyberColorScheme = darkColorScheme(
    background = TrueBlack,
    surface = DarkGrayCard,
    primary = CyberAccent,
    onBackground = TextPrimary,
    onSurface = TextSecondary
)

@Composable
fun OmniGridTheme(content: @Composable () -> Widget) {
    MaterialTheme(
        colorScheme = CyberColorScheme,
        content = content
    )
}
