package com.tuapp.calculadora

import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.tuapp.calculadora.ui.dashboard.ModularDashboard
import com.tuapp.calculadora.ui.system.SessionOrchestrator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * OmniGrid Main Runtime Bootloader.
 * Configura el entorno de hardware inmersivo táctico y arranca de manera
 * directa el ModularDashboard sin pasar por ninguna fachada o PIN de desbloqueo.
 */
class MainActivity : ComponentActivity() {

    private val activityScope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configuración Imersiva de la Ventana para la identidad Cyberdeck (OLED-First)
        window.attributes.layoutInDisplayCutoutMode = 
            WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        hideSystemUI()

        // Inicializar de inmediato la sesión operativa real del Runtime
        SessionOrchestrator.bootstrapSession()

        // Notificar al Bus de Eventos Central que el hardware del Cyberdeck está en línea
        activityScope.launch {
            CoreEventBus.emitEvent(CoreEventBus.RuntimeEvent.SystemBootstrapped(System.currentTimeMillis()))
        }

        setContent {
            // Estilo visual Premium: Fondo negro puro absoluto para ahorro energético de píxeles
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color(0xFF000000) 
            ) {
                // El Dashboard Modular toma el control como Home oficial del Runtime OS
                ModularDashboard(
                    onExitRuntime = {
                        finishAffinity()
                    }
                )
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemUI()
        }
    }

    /**
     * Fuerza al sistema operativo Android a ocultar barras tradicionales de navegación
     * para asegurar una experiencia táctica de consola viva y limpia.
     */
    private fun hideSystemUI() {
        val decorView = window.decorView
        val controller = decorView.windowInsetsController
        if (controller != null) {
            controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
            controller.systemBarsBehavior = 
                WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
}
