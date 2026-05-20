package com.tuapp.calculadora.ui.system

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

// ==========================================
// 1. TINY WAVEFORM (Runtime HUD)
// ==========================================
@Composable
fun TinyWaveform(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")
    
    // Desfasamos las animaciones para que parezca audio/telemetría real
    val bar1 by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "bar1"
    )
    val bar2 by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 0.4f,
        animationSpec = infiniteRepeatable(tween(550, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "bar2"
    )
    val bar3 by infiniteTransition.animateFloat(
        initialValue = 0.5f, targetValue = 0.9f,
        animationSpec = infiniteRepeatable(tween(300, easing = LinearOutSlowInEasing), RepeatMode.Reverse),
        label = "bar3"
    )

    Row(
        modifier = modifier.height(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        WaveformBar(scaleY = bar1)
        WaveformBar(scaleY = bar2)
        WaveformBar(scaleY = bar3)
    }
}

@Composable
private fun WaveformBar(scaleY: Float) {
    Box(
        modifier = Modifier
            .width(3.dp)
            .fillMaxHeight()
            .graphicsLayer { this.scaleY = scaleY } // Transformación en GPU, cero recomposición
            .clip(RoundedCornerShape(50))
            .background(TacticalColors.ActivityPulse.copy(alpha = 0.7f))
    )
}

// ==========================================
// 2. BREATHING GLOW (Activity Visual)
// ==========================================
@Composable
fun BreathingIndicator(modifier: Modifier = Modifier, color: Color = TacticalColors.ActivityPulse) {
    val infiniteTransition = rememberInfiniteTransition(label = "breathing")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine), // Respiración muy lenta y sutil
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(
        modifier = modifier
            .size(6.dp)
            .graphicsLayer { this.alpha = alpha }
            .clip(CircleShape)
            .background(color)
    )
}

// ==========================================
// 3. SCANLINE OVERLAY (Cinematic Effect)
// ==========================================
/**
 * Colocar esto como overlay principal del Scaffold. 
 * Es imperceptible pero le da vida a la pantalla.
 */
@Composable
fun ScanlineOverlay(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "scanline")
    val translationY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f, // Arbitrary large number, canvas will wrap it conceptually
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing), // Lento y constante
            repeatMode = RepeatMode.Restart
        ),
        label = "scanline_anim"
    )

    Canvas(modifier = modifier.fillMaxSize().graphicsLayer { alpha = 0.03f }) {
        val yPos = (translationY % size.height)
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color.Transparent, Color.White, Color.Transparent),
                startY = yPos,
                endY = yPos + 10.dp.toPx()
            ),
            size = size
        )
    }
}
