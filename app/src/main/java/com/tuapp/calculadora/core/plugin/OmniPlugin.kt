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
    UI_DASHBOARD_WIDGET
}

/**
 * Estados del ciclo de vida monitoreados por el Health System.
 */
enum class PluginState {
    IDLE, 
    INITIALIZING, 
    ACTIVE, 
    DEGRADED, 
    SUSPENDED, 
    CRASHED
}

/**
 * Manifiesto del Plugin. Definición estática para el Bootloader.
 */
data class PluginManifest(
    val pluginId: String,
    val version: String,
    val capabilities: Set<PluginCapability>,
    val dependencies: Set<PluginCapability> = emptySet(),
    val priority: Int = 0, // 100 = Critical OS, 0 = Background Utility
    val requiresForeground: Boolean = false
)

/**
 * Contrato Core para todo módulo dentro de OmniGrid.
 */
interface OmniPlugin {
    val manifest: PluginManifest
    val state: StateFlow<PluginState>

    // Lifecycle Hooks
    suspend fun initialize()
    suspend fun start()
    suspend fun stop()
    suspend fun recover() // Degradación elegante o reinicio

    // Capa Dinámica de UI. Si el plugin no tiene UI, retorna Unit.
    @Composable
    fun RenderWidget()
}
