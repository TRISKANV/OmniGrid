package com.tuapp.calculadora.ui.system

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

// ==========================================================================
// EVENTOS NATIVOS DEL SISTEMA OPERATIVO
// ==========================================================================
sealed class OmniEvent {
    data class TelemetryEmitted(val entry: TelemetryEntry) : OmniEvent()
    data class SystemStressChanged(val oldLevel: SystemStressLevel, val newLevel: SystemStressLevel) : OmniEvent()
    data class PluginStateChanged(val pluginId: String, val isActive: Boolean) : OmniEvent()
    data class HardwareWarning(val message: String) : OmniEvent()
    
    // Envoltorio puente para los eventos que vienen de los plugins
    data class PluginSystemEvent(val type: String, val payload: Map<String, Any>) : OmniEvent()
}

// ==========================================================================
// EVENTOS DE PLUGINS (Como el SecureVaultPlugin)
// ==========================================================================
data class SystemEvent(
    val type: String,
    val payload: Map<String, Any> = emptyMap()
)

// ==========================================================================
// CORE EVENT BUS
// ==========================================================================
object CoreEventBus {
    private val _events = MutableSharedFlow<OmniEvent>(extraBufferCapacity = 64)
    val events = _events.asSharedFlow()

    // Método Legacy usado por el motor de IA y Telemetría del OS
    fun publish(event: OmniEvent) {
        _events.tryEmit(event)
    }

    // Método Nuevo usado por los Plugins modulares (SecureVault, etc.)
    fun emit(event: SystemEvent) {
        _events.tryEmit(OmniEvent.PluginSystemEvent(event.type, event.payload))
    }
}
