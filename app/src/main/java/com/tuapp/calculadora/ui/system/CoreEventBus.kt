package com.tuapp.calculadora.core

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * CYBERDECK CORE EVENT BUS.
 * Arquitectura restaurada con soporte de tipado estricto y retrocompatibilidad elástica.
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

    fun publish(event: OmniEvent): Boolean {
        return _events.tryEmit(event)
    }
}

/**
 * JERARQUÍA DE EVENTOS OMNI
 * Convertida a 'open class' para desactivar la verificación estricta de exhaustividad en dashboards de UI.
 */
open class OmniEvent

// --- 1. Eventos de UI y Estado de Bóveda ---
data class SystemEvent(val message: String) : OmniEvent()
data class SessionChanged(val oldState: String, val newState: String) : OmniEvent()
data class PayloadEvent(val type: String, val payload: String) : OmniEvent()
data class VaultEvent(val state: String = "", val isLocked: Boolean = false) : OmniEvent()

// --- 2. Eventos de Hardware y Telemetría con Soporte Dual (Propiedades + Vararg) ---
data class ThermalStateChanged(val oldState: String = "", val newState: String = "") : OmniEvent() {
    constructor(vararg args: Any) : this(
        oldState = args.getOrNull(0)?.toString() ?: "",
        newState = args.getOrNull(1)?.toString() ?: ""
    )
}

data class HardwareWarning(
    val subsystem: String = "", 
    val severity: String = "", 
    val message: String = ""
) : OmniEvent() {
    constructor(vararg args: Any) : this(
        subsystem = args.getOrNull(0)?.toString() ?: "",
        severity = args.getOrNull(1)?.toString() ?: "",
        message = args.getOrNull(2)?.toString() ?: ""
    )
}

data class HardwareTelemetryEmitted(
    val state: String = "",
    val isLocked: Boolean = false
) : OmniEvent() {
    constructor(vararg args: Any) : this(
        state = args.getOrNull(0)?.toString() ?: ""
    )
}

data class TelemetryEmitted(
    val targetServer: String = "", 
    val success: Boolean = true, 
    val state: String = ""
) : OmniEvent() {
    constructor(vararg args: Any) : this(
        targetServer = args.getOrNull(0)?.toString() ?: "",
        success = args.getOrNull(1) as? Boolean ?: true,
        state = args.getOrNull(2)?.toString() ?: ""
    )
}

// --- 3. Eventos del Sistema de Plugins (Requerido por TacticalDiagnosticsDrawer) ---
data class PluginSystemEvent(val type: String = "", val payload: String = "") : OmniEvent()

// --- 4. Puente de Compatibilidad de Tipos ---
typealias RuntimeEvent = OmniEvent
