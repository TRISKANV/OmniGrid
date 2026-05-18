package com.cyber.omnigrid.core.designsystem.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.cyber.omnigrid.core.designsystem.theme.BorderGray
import com.cyber.omnigrid.core.designsystem.theme.DarkGrayCard

/**
 * Contenedor universal de la aplicación.
 * Implementa las líneas finas estilo HUD y animaciones de tamaño internas automáticas.
 */
@Composable
fun CyberCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(12.dp)
    
    val cardModifier = modifier
        .clip(shape)
        .border(1.dp, BorderGray, shape)
        .animateContentSize() // Animación suave cuando los elementos internos cambian o se expanden
    
    val finalModifier = if (onClick != null) {
        cardModifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null // Eliminamos el ripple genérico para mantener el minimalismo
        ) { onClick() }
    } else {
        cardModifier
    }

    Surface(
        modifier = finalModifier,
        shape = shape,
        color = DarkGrayCard
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}
