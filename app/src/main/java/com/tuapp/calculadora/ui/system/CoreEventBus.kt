package com.tuapp.calculadora.core

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * CYBERDECK CORE EVENT BUS.
 * Bus de eventos unificado. Arquitectura restaurada con retrocompatibilidad táctica.
 */
object CoreEventBus {

    private val _events = MutableSharedFlow<OmniEvent>(
        replay = 0, 
        extraBufferCapacity = 64
    )
    val events: SharedFlow<OmniEvent> = _events.asSharedFlow()

    suspend fun emit(event: OmniEvent) {
        _events.emit(event)
    }

    suspend fun emitEvent(event: OmniEvent) {
        _events.emit(event)
    }

    fun tryEmitEvent(event: OmniEvent): Boolean {
        return _events.tryEmit(event)
    }

    // Alias defensivo por si algún subsistema aún intenta usar sintaxis estilo Rx
    fun publish(event: OmniEvent): Boolean {
        return _events.tryEmit(event)
    }
}

/**
 * JERARQUÍA OFICIAL DE EVENTOS
 */
sealed class OmniEvent

// --- 1. Eventos de UI y Estado de Bóveda ---
data class SystemEvent(val message: String) : OmniEvent()
data class SessionChanged(val oldState: String, val newState: String) : OmniEvent()
data class PayloadEvent(val type: String, val payload: String) : OmniEvent()
data class VaultEvent(val state: String) : OmniEvent()

// --- 2. Eventos de Hardware y Telemetría (Restaurados) ---
// Usamos vararg para absorber cualquier argumento (ej. severity, subsystem) que envíes desde RuntimeIntelligenceEngine.
class ThermalStateChanged(vararg val args: Any) : OmniEvent()
class HardwareWarning(vararg val args: Any) : OmniEvent()
class HardwareTelemetryEmitted(vararg val args: Any) : OmniEvent()
class TelemetryEmitted(vararg val args: Any) : OmniEvent()

// --- 3. Puente de Compatibilidad ---
// SessionOrchestrator sigue buscando "RuntimeEvent". Este alias compila la solución sin tocar ese archivo.
typealias RuntimeEvent = OmniEvent
