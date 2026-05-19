package com.cyber.omnigrid.core.designsystem.theme

import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

val TrueBlack = Color(0xFF0A0A0A)

@Composable
fun OmniGridTheme(content: @Composable () -> Unit) {
    Log.d("OMNI_BOOTSTRAP", "[THEME] Aplicando OmniGridTheme a la jerarquía de composición.")
    
    // Forzamos una superficie sólida ocupando toda la pantalla para mitigar renderizados transparentes
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = TrueBlack
    ) {
        content()
    }
}
