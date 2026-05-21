package com.tuapp.calculadora.ui.system

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

// Estructura de datos requerida por el Vault y la Timeline
data class SystemEvent(
    val type: String,
    val payload: Map<String, Any> = emptyMap()
)

object CoreEventBus {
    // Buffer para no perder eventos tácticos de telemetría
    private val _events = MutableSharedFlow<SystemEvent>(extraBufferCapacity = 64)
    val events = _events.asSharedFlow()

    // Método expuesto para que los plugins inyecten eventos sin necesitar scopes
    fun emit(event: SystemEvent) {
        _events.tryEmit(event)
    }
}
