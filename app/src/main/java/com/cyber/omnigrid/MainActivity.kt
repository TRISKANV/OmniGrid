package com.tuapp.calculadora

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import com.tuapp.calculadora.core.plugin.RuntimePluginManager
import com.tuapp.calculadora.core.plugin.TelemetryPlugin
import com.tuapp.calculadora.ui.dashboard.DiagnosticsPlugin
import com.tuapp.calculadora.ui.dashboard.ModularDashboard
import com.tuapp.calculadora.ui.system.SessionOrchestrator

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Inmersión Total (OLED-First Cyberdeck)
        // Permite que la UI ocupe la pantalla completa, ignorando la barra de estado y navegación.
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // 2. Levantar la identidad de sesión del operador de forma segura
        SessionOrchestrator.bootstrapSession()

        // 3. Registrar los Plugins Reales en el Kernel
        // El orden de registro no importa, el Kernel los ordenará por la `priority` de sus Manifiestos.
        RuntimePluginManager.registerPlugin(TelemetryPlugin(applicationContext))
        RuntimePluginManager.registerPlugin(DiagnosticsPlugin())

        // 4. Boot Ecosystem (Ignition)
        // Levanta hilos de aislamiento, resuelve dependencias e inicia el Health Monitor.
        RuntimePluginManager.bootEcosystem()

        // 5. Entregar el control visual al Dashboard Dinámico
        setContent {
            ModularDashboard()
        }
    }
}
