package com.cyber.omnigrid.feature.automation.domain.engine

import com.cyber.omnigrid.feature.automation.domain.model.DuckyAction
import kotlinx.coroutines.flow.Flow

// Estados de ejecución para renderizar en el Dashboard
sealed class EngineState {
    data object Idle : EngineState()
    data class Running(val currentActionIndex: Int, val totalActions: Int, val log: String) : EngineState()
    data class Success(val durationMs: Long) : EngineState()
    data class Error(val message: String) : EngineState()
}

/**
 * Contrato universal para cualquier método de inyección de payloads.
 */
interface PayloadEngine {
    val engineName: String
    val isRootRequired: Boolean
    val isBluetoothRequired: Boolean

    // Toma las acciones normalizadas y devuelve el progreso en tiempo real
    fun execute(actions: List<DuckyAction>): Flow<EngineState>
    
    // Para el botón de "Abortar Misión"
    fun cancel()
}
