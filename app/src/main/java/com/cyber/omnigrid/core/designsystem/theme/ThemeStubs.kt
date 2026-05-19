package com.cyber.omnigrid.core.designsystem.theme

import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

// Paleta de colores Flagship (Nothing OS / Flipper Zero hybrid)
object OmniColors {
    val Background = Color(0xFF050505)       // Negro Puro Absoluto
    val SurfaceCard = Color(0xFF111111)      // Gris carbón profundo para CyberCards
    val BorderMuted = Color(0xFF222222)      // Bordes limpios de un píxel
    val BorderActive = Color(0xFF444444)     // Bordes para estados enfocados
    
    // Acentos funcionales de matriz
    val AccentGreen = Color(0xFF00FF66)      // Estado Online / OK
    val AccentCyan = Color(0xFF00E5FF)       // Ejecución / Payload
    val AccentAmber = Color(0xFFFF9100)      // Alertas / Telemetría
    val TextPrimary = Color(0xFFFFFFFF)
    val TextSecondary = Color(0xFF888888)    // Sintaxis corregida
}

@Composable
fun OmniGridTheme(content: @Composable () -> Unit) {
    Log.d("OMNI_THEME", "Cargando paleta visual premium en el árbol de composición.")
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = OmniColors.Background
    ) {
        content()
    }
}
