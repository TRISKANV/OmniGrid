package com.tuapp.calculadora.ui.dashboard

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.tuapp.calculadora.ui.system.*

@Composable
fun OmniOSCockpit(content: @Composable () -> Unit) {
    val adaptiveConfig by RuntimeIntelligenceEngine.adaptiveConfig.collectAsState()
    val stressLevel by RuntimeIntelligenceEngine.stressLevel.collectAsState()
    val haptics = LocalHapticFeedback.current

    // Efecto físico al cambiar de estado de estrés
    LaunchedEffect(stressLevel) {
        if (adaptiveConfig.enableHaptics && stressLevel != SystemStressLevel.NOMINAL) {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    CompositionLocalProvider(LocalAdaptiveConfig provides adaptiveConfig) {
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFF030303))) {
            
            // 1. AMBIENT RUNTIME PULSE (Fondo vivo)
            AmbientRuntimePulse(stressLevel, adaptiveConfig)

            // 2. EL CONTENIDO DE LA APP (Dashboard, etc.)
            Box(modifier = Modifier.fillMaxSize()) {
                content()
            }

            // 3. SYSTEM STRESS OVERLAY (Vignette de emergencia, ignora toques)
            SystemStressOverlay(stressLevel)
        }
    }
}

@Composable
private fun AmbientRuntimePulse(stress: SystemStressLevel, config: AdaptiveUIConfig) {
    val infiniteTransition = rememberInfiniteTransition()
    
    // Si la escala de animación es 0 (CRITICAL), pausamos el pulso
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f * config.ambientGlowOpacity,
        targetValue = 0.8f * config.ambientGlowOpacity,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = (2000 / (config.animationScale.takeIf { it > 0 } ?: 0.01f)).toInt(), easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        )
    )

    val baseColor by animateColorAsState(
        targetValue = when (stress) {
            SystemStressLevel.NOMINAL -> Color(0xFF001A22)
            SystemStressLevel.ELEVATED -> Color(0xFF221500)
            SystemStressLevel.SEVERE -> Color(0xFF220500)
            SystemStressLevel.CRITICAL -> Color(0xFF330000)
        },
        animationSpec = tween(1500)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { alpha = pulseAlpha } // Rendering directo en GPU
            .background(
                Brush.radialGradient(
                    colors = listOf(baseColor, Color.Transparent),
                    radius = 1500f
                )
            )
    )
}

@Composable
private fun SystemStressOverlay(stress: SystemStressLevel) {
    val targetAlpha = if (stress == SystemStressLevel.CRITICAL) 0.5f else 0f
    val alphaState by animateFloatAsState(targetValue = targetAlpha, animationSpec = tween(500))

    if (alphaState > 0f) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = alphaState }
                .drawBehind {
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(Color.Transparent, Color.Red.copy(alpha = 0.4f)),
                            radius = size.width * 0.8f
                        )
                    )
                }
        )
    }
}
