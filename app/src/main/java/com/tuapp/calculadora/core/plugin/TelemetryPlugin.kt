package com.tuapp.calculadora.core.plugin

import android.content.Context
import androidx.compose.runtime.Composable
import com.tuapp.calculadora.ui.system.RuntimeIntelligenceEngine
import com.tuapp.calculadora.ui.system.RuntimeTelemetryManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * EL DAEMON SENSORIAL.
 * Plugin de ejecución en segundo plano (Headless) responsable de 
 * alimentar el ecosistema con métricas de hardware e inteligencia reactiva.
 */
class TelemetryPlugin(private val context: Context) : OmniPlugin {

    // 1. Manifiesto del Plugin
    override val manifest = PluginManifest(
        pluginId = "core.telemetry.daemon",
        version = "1.0.0",
        capabilities = setOf(
            PluginCapability.RUNTIME_TELEMETRY, 
            PluginCapability.THERMAL_ANALYSIS
        ),
        priority = 90, // Alta prioridad (inicia justo después del módulo de Diagnósticos)
        requiresForeground = false // Opera sin necesidad de estar en pantalla
    )

    // 2. Estado del Ciclo de Vida
    private val _state = MutableStateFlow(PluginState.IDLE)
    override val state: StateFlow<PluginState> = _state.asStateFlow()

    // --- LIFECYCLE HOOKS ---

    override suspend fun initialize() {
        _state.value = PluginState.INITIALIZING
        val pluginScope = CoroutineScope(Dispatchers.IO)
        
        // Inicializa la infraestructura de bajo nivel
        RuntimeTelemetryManager.initialize(context, pluginScope)
        RuntimeIntelligenceEngine.initialize(pluginScope)
    }

    override suspend fun start() {
        _state.value = PluginState.RUNNING
        // Inicia el parseo del Kernel Linux y la RAM
        RuntimeTelemetryManager.startMonitoring()
    }

    override suspend fun stop() {
        RuntimeTelemetryManager.stopMonitoring()
        _state.value = PluginState.DISABLED
    }

    override suspend fun recover() {
        stop()
        start()
    }

    // --- UI DYNAMIC RENDERING ---

    @Composable
    override fun RenderWidget() {
        // Al no tener la capacidad PluginCapability.UI_DASHBOARD_WIDGET en su manifiesto, 
        // el RuntimePluginManager nunca llamará a este método. 
        // Es un componente 100% silencioso.
    }
}
