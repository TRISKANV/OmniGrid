package com.cyber.omnigrid.core.designsystem.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.cyber.omnigrid.core.designsystem.theme.BorderGray
import com.cyber.omnigrid.core.designsystem.theme.CyberAccent

@Composable
fun CyberHudProgress(
    progress: Float?, // null para estado indeterminado
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "hud_loop")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "rotation"
    )

    Canvas(modifier = modifier.size(48.dp)) {
        // Círculo de fondo estático (Métrica HUD)
        drawCircle(
            color = BorderGray,
            style = Stroke(width = 2.dp.toPx())
        )

        // Línea de progreso activa
        if (progress != null) {
            drawArc(
                color = CyberAccent,
                startAngle = -90f,
                sweepAngle = progress * 360f,
                useCenter = false,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )
        } else {
            // Animación indeterminada limpia
            drawArc(
                color = CyberAccent,
                startAngle = rotation,
                sweepAngle = 90f,
                useCenter = false,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )
        }
    }
}
