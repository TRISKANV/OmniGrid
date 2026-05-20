package com.tuapp.calculadora.ui.system

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// ==========================================
// 1. COLOR SYSTEM (OLED First)
// ==========================================
object TacticalColors {
    val OledBlack = Color(0xFF000000)
    val SurfaceDark = Color(0xFF0A0B0C)
    val SurfaceGlass = Color(0x660A0B0C) // Translucent for glass effects
    val BorderGlass = Color(0x1AFFFFFF) // 10% white for subtle edge
    
    val TextPrimary = Color(0xFFE2E2E2)
    val TextSecondary = Color(0xFF7A7A7A)
    
    val ActivityPulse = Color(0xFFE2E2E2) // Subtle white/grey for activity
    val SystemWarning = Color(0xFFD6A24A) // Muted amber for warnings
}

// ==========================================
// 2. MOTION SYSTEM (Hardware-level easing)
// ==========================================
object TacticalMotion {
    // Físico, rápido al inicio, suave al final. Ideal para microinteracciones.
    val SpringSnappy = spring<Float>(
        dampingRatio = 0.8f,
        stiffness = Spring.StiffnessMedium
    )
    
    // Suave y fluido. Ideal para expansiones modulares o modales.
    val SpringSmooth = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessLow
    )
}

// ==========================================
// 3. DEPTH & GLASS SYSTEM
// ==========================================
/**
 * Modificador para paneles HUD y modales. 
 * Crea un efecto "Glass" sutil ideal para fondos OLED.
 */
fun Modifier.tacticalGlass(
    cornerRadius: Float = 16f
): Modifier = this
    .clip(RoundedCornerShape(cornerRadius.dp))
    .background(TacticalColors.SurfaceGlass)
    .border(
        width = 1.dp,
        color = TacticalColors.BorderGlass,
        shape = RoundedCornerShape(cornerRadius.dp)
    )
