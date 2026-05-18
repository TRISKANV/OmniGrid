package com.cyber.omnigrid.core.designsystem.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.cyber.omnigrid.core.designsystem.theme.BorderGray
import com.cyber.omnigrid.core.designsystem.theme.CyberAccent
import com.cyber.omnigrid.core.designsystem.theme.DarkGrayCard

/**
 * Contenedor universal evolucionado.
 * Implementa micro-interacciones táctiles, glassmorphism sutil y bordes reactivos.
 */
@Composable
fun CyberCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(12.dp)
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Animación de borde: Si se presiona, brilla con el acento Cyber
    val borderColor = if (isPressed) CyberAccent else BorderGray

    val cardModifier = modifier
        .clip(shape)
        .border(1.dp, borderColor, shape)
        .animateContentSize()

    val finalModifier = if (onClick != null) {
        cardModifier.clickable(
            interactionSource = interactionSource,
            indication = null 
        ) { onClick() }
    } else {
        cardModifier
    }

    Surface(
        modifier = finalModifier,
        shape = shape,
        color = Color.Transparent // Permitimos ver el fondo de la brocha trasera
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            DarkGrayCard,
                            if (isPressed) Color(0xFF0F0F12) else Color(0xFF050505)
                        )
                    )
                )
                .padding(16.dp)
        ) {
            content()
        }
    }
}
