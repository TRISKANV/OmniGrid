package com.tuapp.calculadora.core.plugin

import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.StateFlow

/**
 * Capacidades negociables del ecosistema.
 */
enum class PluginCapability {
    HARDWARE_MONITORING,
    RUNTIME_TELEMETRY,
    SECURE_STORAGE,
    PAYLOAD_EXECUTION,
    NETWORK_OBSERVABILITY,
    THERMAL_ANALYSIS,
    EVENT_STREAMING,
    DIAGNOSTICS,
    UI_DASHBOARD_WIDGET,
    STRESS_TESTING // Nueva capacidad para el módulo de caos
}

/**
 * Estados del Supervisor Runtime (Micro-kernel).
 */
enum class PluginState {
    IDLE, 
    INITIALIZING, 
    RUNNING,      // Reemplaza a ACTIVE
    DEGRADED, 
    CRASHED, 
    RECOVERING,   // Intento de reinicio
    DISABLED      // Módulo expulsado por fallo crítico recurrente
}

/**
 * Manifiesto del Plugin.
 */
data class PluginManifest(
    val pluginId: String,
    val version: String,
    val capabilities: Set<PluginCapability>,
    val dependencies: Set<PluginCapability> = emptySet(),
    val priority: Int = 0,
    val requiresForeground: Boolean = false
)

/**
 * Contrato Core de OmniGrid.
 */
interface OmniPlugin {
    val manifest: PluginManifest
    val state: StateFlow<PluginState>

    // Lifecycle Hooks controlados por el Sandbox Layer
    suspend fun initialize()
    suspend fun start()
    suspend fun stop()
    suspend fun recover()

    @Composable
    fun RenderWidget()
}
