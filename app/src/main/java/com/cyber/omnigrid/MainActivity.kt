package com.tuapp.calculadora

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import com.tuapp.calculadora.core.plugin.ChaosPlugin
import com.tuapp.calculadora.core.plugin.RuntimePluginManager
import com.tuapp.calculadora.core.plugin.TelemetryPlugin
import com.tuapp.calculadora.ui.dashboard.DiagnosticsPlugin
import com.tuapp.calculadora.ui.dashboard.ModularDashboard
import com.tuapp.calculadora.ui.system.SessionOrchestrator

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        SessionOrchestrator.bootstrapSession()

        // 1. Registramos el Daemon
        RuntimePluginManager.registerPlugin(TelemetryPlugin(applicationContext))
        // 2. Registramos la vista de Diagnósticos (Paciente Cero)
        RuntimePluginManager.registerPlugin(DiagnosticsPlugin())
        // 3. Registramos la Ingeniería del Caos
        RuntimePluginManager.registerPlugin(ChaosPlugin())

        // Ignition
        RuntimePluginManager.bootEcosystem()

        setContent {
            ModularDashboard()
        }
    }
}
