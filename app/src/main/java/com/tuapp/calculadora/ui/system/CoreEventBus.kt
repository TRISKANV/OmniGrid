package com.tuapp.calculadora.core.system

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * CYBERDECK CORE EVENT BUS.
 * Bus de eventos unificado de alta velocidad y concurrente para OmniGrid.
 * Actúa como el sistema nervioso del Runtime OS, permitiendo la comunicación reactiva real 
 * entre SecureVault, Cargas útiles, Telemetría e Interfaz Gráfica sin acoplamiento rígido.
 */
object CoreEventBus {

    /**
     * Jerarquía de Eventos Reales que ocurren dentro de la plataforma táctica.
     */
    sealed class RuntimeEvent {
        data class SystemBootstrapped(val timestamp: Long) : RuntimeEvent()
        data class SessionChanged(val sessionId: String, val state: String) : RuntimeEvent()
        data class TelemetryHeartbeat(val timestamp: Long) : RuntimeEvent()
        data class SecureVaultLockChanged(val isLocked: Boolean) : RuntimeEvent()
        data class PayloadDispatched(val payloadId: String, val targetServer: String, val success: Boolean) : RuntimeEvent()
        data class HardwareAnomalyDetected(val subsystem: String, val severity: String) : RuntimeEvent()
    }

    // Flujo caliente (SharedFlow) configurado para soportar ráfagas asíncronas sin retención obsoleta
    private val _events = MutableSharedFlow<RuntimeEvent>(
        replay = 0, 
        extraBufferCapacity = 64
    )
    val events: SharedFlow<RuntimeEvent> = _events.asSharedFlow()

    /**
     * Emite un evento en tiempo real hacia todos los subsistemas o pantallas suscritas.
     */
    async fun emitEvent(event: RuntimeEvent) {
        _events.emit(event)
    }

    /**
     * Emite un evento de manera síncrona/inmediata desde contextos donde no es posible usar suspend.
     */
    fun tryEmitEvent(event: RuntimeEvent): Boolean {
        return _events.tryEmit(event)
    }
}
