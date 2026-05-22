package com.tuapp.calculadora.core

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * CYBERDECK CORE EVENT BUS.
 * Bus de eventos unificado de alta velocidad para OmniGrid.
 * Restablecido al paquete .core para resolver la integridad de las importaciones.
 */
object CoreEventBus {

    // Flujo caliente reactivo configurado para OmniEvent
    private val _events = MutableSharedFlow<OmniEvent>(
        replay = 0, 
        extraBufferCapacity = 64
    )
    val events: SharedFlow<OmniEvent> = _events.asSharedFlow()

    /**
     * Emite un evento en tiempo real (Función nativa esperada por SecureVaultPlugin).
     */
    suspend fun emit(event: OmniEvent) {
        _events.emit(event)
    }

    /**
     * Alias de emisión por compatibilidad con firmas alternativas.
     */
    suspend fun emitEvent(event: OmniEvent) {
        _events.emit(event)
    }

    /**
     * Emite un evento de manera síncrona/inmediata si es necesario.
     */
    fun tryEmitEvent(event: OmniEvent): Boolean {
        return _events.tryEmit(event)
    }
}

/**
 * JERARQUÍA OFICIAL DE OMNIEVENT.
 * Definidos como clases top-level dentro de .core para que las importaciones masivas funcionen.
 */
sealed class OmniEvent

// 1. Manejado en TacticalDiagnosticsDrawer y SecureVaultPlugin (requiere 'message')
data class SystemEvent(val message: String) : OmniEvent()

// 2. Manejado en TacticalDiagnosticsDrawer (requiere 'oldState' y 'newState')
data class SessionChanged(val oldState: String, val newState: String) : OmniEvent()

// 3. Manejado en TacticalDiagnosticsDrawer (requiere 'type' y 'payload')
data class PayloadEvent(val type: String, val payload: String) : OmniEvent()

// 4. Manejado en TacticalDiagnosticsDrawer y vistas de estado (requiere 'state')
data class VaultEvent(val state: String) : OmniEvent()
