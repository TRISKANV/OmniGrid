package com.tuapp.calculadora.ui.system

import com.tuapp.calculadora.ui.system.model.HardwareState
import com.tuapp.calculadora.ui.system.model.ThermalState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

// ==========================================================================
// EVENTOS NATIVOS REALES DEL SISTEMA OPERATIVO
// ==========================================================================
sealed class OmniEvent {
    // Consume el estado real de hardware y runtime
    data class TelemetryEmitted(val state: HardwareState) : OmniEvent()
    // Registra transiciones térmicas reales detectadas por el sistema
    data class ThermalStateChanged(val oldState: ThermalState, val newState: ThermalState) : OmniEvent()
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
    private val _events = MutableSharedFlow<OmniEvent>(extraBufferCapacity = 128)
    val events = _events.asSharedFlow()

    // Publicación atómica de eventos del sistema central
    fun publish(event: OmniEvent) {
        _events.tryEmit(event)
    }

    // Emisión desde módulos/plugins independientes
    fun emit(event: SystemEvent) {
        _events.tryEmit(OmniEvent.PluginSystemEvent(event.type, event.payload))
    }
}
