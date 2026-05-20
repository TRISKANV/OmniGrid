package com.tuapp.calculadora.ui.system

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

// ==========================================================================
// 1. INTERACCIÓN TÁCTIL PREMIUM
// ==========================================================================
fun Modifier.tacticalClick(
    enabled: Boolean = true,
    onClick: () -> Unit
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    // Animación de escala sutil al presionar (simula amortiguación física pesada)
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = 0.7f),
        label = "TacticalClickScale"
    )

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(
            interactionSource = interactionSource,
            indication = null, // Desactivamos el ripple nativo circular para conservar la estética sci-fi limpia
            enabled = enabled,
            onClick = onClick
        )
}

// ==========================================================================
// 2. CAPA DE ESCANEO AMBIENTAL (Scanlines de monitor CRT de fondo)
// ==========================================================================
@Composable
fun ScanlineOverlay(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val lineSpacing = 8.dp.toPx()
        
        var y = 0f
        while (y < canvasHeight) {
            drawLine(
                color = Color(0x06FFFFFF),
                start = Offset(0f, y),
                end = Offset(canvasWidth, y),
                strokeWidth = 1.dp.toPx()
            )
            y += lineSpacing
        }
    }
}

// ==========================================================================
// 3. BREATHING INDICATOR (Pulso de estado nominal)
// ==========================================================================
@Composable
fun BreathingIndicator(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "IndicatorPulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "AlphaPulse"
    )

    Box(
        modifier = modifier
            .size(6.dp)
            .graphicsLayer { this.alpha = alpha }
            .background(Color(0xFF00FF66), RoundedCornerShape(50))
    )
}

// ==========================================================================
// 4. TINY WAVEFORM (Micro-animación de telemetría de hilos)
// ==========================================================================
@Composable
fun TinyWaveform(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "WaveformAnim")
    
    Row(
        modifier = modifier.height(18.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        repeat(4) { index ->
            val duration = remember { listOf(500, 750, 600, 850)[index] }
            val heightScale by infiniteTransition.animateFloat(
                initialValue = 0.2f,
                targetValue = 1.0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(duration, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "BarScale"
            )

            Box(
                modifier = Modifier
                    .width(2.dp)
                    .fillMaxHeight(heightScale)
                    .background(TacticalColors.TextSecondary.copy(alpha = 0.6f))
            )
        }
    }
}
