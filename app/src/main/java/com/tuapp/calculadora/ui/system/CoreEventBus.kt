package com.tuapp.calculadora.ui.system

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

sealed class OmniEvent {
    data class TelemetryEmitted(val entry: TelemetryEntry) : OmniEvent()
    data class SystemStressChanged(val oldLevel: SystemStressLevel, val newLevel: SystemStressLevel) : OmniEvent()
    data class PluginStateChanged(val pluginId: String, val isActive: Boolean) : OmniEvent()
    data class HardwareWarning(val message: String) : OmniEvent()
}

object CoreEventBus {
    // SharedFlow actúa como un bus de transmisión de SO. extraBufferCapacity evita cuellos de botella.
    private val _events = MutableSharedFlow<OmniEvent>(extraBufferCapacity = 64)
    val events = _events.asSharedFlow()

    fun publish(event: OmniEvent) {
        _events.tryEmit(event)
    }
}
