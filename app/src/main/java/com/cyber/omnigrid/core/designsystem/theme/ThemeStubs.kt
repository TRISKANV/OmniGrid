package com.cyber.omnigrid.core.designsystem.theme

import android.util.Log
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer

// EXPOSICIÓN GLOBAL
val TrueBlack = Color(0xFF030303) // Aún más profundo para OLED

object OmniColors {
    val Background = TrueBlack
    val SurfaceCard = Color(0xFF0D0D0D)      // Gris levísimamente elevado
    val BorderMuted = Color(0xFF1F1F1F)      // Bordes ultra limpios
    val BorderActive = Color(0xFF333333)     // Hover/Press
    
    val AccentGreen = Color(0xFF00FF66)      // Online
    val AccentCyan = Color(0xFF00E5FF)       // Exec
    val AccentAmber = Color(0xFFFF9100)      // Warn
    
    val TextPrimary = Color(0xFFEBEBEB)      // Blanco crudo, no absoluto
    val TextSecondary = Color(0xFF7A7A7A)    // Gris balanceado para tracking
}

// Físicas del sistema (Spring-based)
object OmniMotion {
    val TacticalBouncy = spring<Float>(dampingRatio = 0.65f, stiffness = 600f)
    val SmoothTransition = spring<Float>(dampingRatio = 0.8f, stiffness = 300f)
}

// Modificador Táctil Premium (Hardware feel sin ripple exagerado)
fun Modifier.tacticalClick(onClick: () -> Unit) = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = OmniMotion.TacticalBouncy,
        label = "tactical_scale"
    )

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(
            interactionSource = interactionSource,
            indication = null, // Desactiva el ripple nativo feo de Android
            onClick = onClick
        )
}

@Composable
fun OmniGridTheme(content: @Composable () -> Unit) {
    Log.d("OMNI_THEME", "Cargando Motor Visual y Físicas.")
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = OmniColors.Background
    ) {
        content()
    }
}
