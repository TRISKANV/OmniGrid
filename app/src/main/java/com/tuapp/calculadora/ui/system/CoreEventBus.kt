package com.tuapp.calculadora.ui.system

import com.tuapp.calculadora.ui.system.model.HardwareState
import com.tuapp.calculadora.ui.system.model.ThermalState
import com.tuapp.calculadora.ui.system.model.SystemStressLevel
import com.tuapp.calculadora.ui.system.model.TelemetryEntry
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

// ==========================================================================
// EVENTOS NATIVOS DEL SISTEMA OPERATIVO
// ==========================================================================
sealed class OmniEvent {
    // --- EVENTOS NUEVOS (Fase Real Hardware) ---
    data class HardwareTelemetryEmitted(val state: HardwareState) : OmniEvent()
    data class ThermalStateChanged(val oldState: ThermalState, val newState: ThermalState) : OmniEvent()
    data class HardwareWarning(val message: String) : OmniEvent()
    
    // --- EVENTOS LEGACY (Para retrocompatibilidad de la UI) ---
    data class TelemetryEmitted(val entry: TelemetryEntry) : OmniEvent()
    data class SystemStressChanged(val oldLevel: SystemStressLevel, val newLevel: SystemStressLevel) : OmniEvent()
    
    // --- EVENTOS DE PLUGINS ---
    data class PluginStateChanged(val pluginId: String, val isActive: Boolean) : OmniEvent()
    data class PluginSystemEvent(val type: String, val payload: Map<String, Any>) : OmniEvent()
}

// ==========================================================================
// EVENTOS DE PLUGINS MODULARES
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

    fun publish(event: OmniEvent) {
        _events.tryEmit(event)
    }

    fun emit(event: SystemEvent) {
        _events.tryEmit(OmniEvent.PluginSystemEvent(event.type, event.payload))
    }
}
