package com.tuapp.calculadora

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import com.omnigrid.payload.di.PayloadBootloader
import com.omnigrid.payload.runtime.events.OmniRuntimeEvent
import com.omnigrid.payload.runtime.events.OmniRuntimeEventBus
import com.omnigrid.payload.runtime.events.OmniTelemetryBridge
import com.omnigrid.payload.runtime.events.OmniTimelineBridge
import com.omnigrid.payload.runtime.events.TelemetryMetric
import com.tuapp.calculadora.core.plugin.ChaosPlugin
import com.tuapp.calculadora.core.plugin.PayloadRuntimePlugin
import com.tuapp.calculadora.core.plugin.RuntimePluginManager
import com.tuapp.calculadora.core.plugin.TelemetryPlugin
import com.tuapp.calculadora.ui.dashboard.DiagnosticsPlugin
import com.tuapp.calculadora.ui.dashboard.ModularDashboard
import com.tuapp.calculadora.ui.system.SessionOrchestrator

class MainActivity : ComponentActivity() {

    // Referencia global al plugin táctico
    private lateinit var payloadPlugin: PayloadRuntimePlugin

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        SessionOrchestrator.bootstrapSession()

        // --- 1. INICIALIZACIÓN DEL MÓDULO PAYLOAD (BOOTLOADER) ---
        payloadPlugin = PayloadBootloader.boot(
            context = applicationContext,
            coreEventBus = object : OmniRuntimeEventBus {
                override suspend fun emit(event: OmniRuntimeEvent) {
                    Log.d("OmniEventBus", "Evento recibido: $event")
                }
            },
            telemetry = object : OmniTelemetryBridge {
                override fun record(metric: TelemetryMetric) {
                    Log.d("OmniTelemetry", "Métrica: ${metric.key} = ${metric.value}")
                }
            },
            timeline = object : OmniTimelineBridge {
                override fun record(moduleId: String, eventType: String, label: String, timestamp: Long) {
                    Log.d("OmniTimeline", "[$moduleId] $eventType: $label")
                }
            }
        )

        // --- 2. REGISTRO EN EL KERNEL DE OMNIGRID ---
        RuntimePluginManager.registerPlugin(TelemetryPlugin(applicationContext))
        RuntimePluginManager.registerPlugin(DiagnosticsPlugin())
        RuntimePluginManager.registerPlugin(ChaosPlugin())
        
        // ¡Registramos nuestra nueva bestia!
        RuntimePluginManager.registerPlugin(payloadPlugin)

        // Ignition
        RuntimePluginManager.bootEcosystem()

        setContent {
            ModularDashboard()
        }
    }
}
