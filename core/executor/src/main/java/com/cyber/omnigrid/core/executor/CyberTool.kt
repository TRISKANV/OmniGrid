package com.cyber.omnigrid.core.executor

import kotlinx.coroutines.flow.Flow

// La base para CUALQUIER herramienta futura
interface CyberTool<Config, Result> {
    val id: String
    val name: String
    val isRootRequired: Boolean
    
    // Todo devuelve un Flow para que la UI reaccione en tiempo real
    fun execute(config: Config): Flow<ToolExecutionState<Result>>
    fun stop()
}

// Estados universales para el renderizado de la UI
sealed class ToolExecutionState<out T> {
    data object Idle : ToolExecutionState<Nothing>()
    data class Running(val progress: Float? = null, val logLine: String? = null) : ToolExecutionState<Nothing>()
    data class Success<T>(val data: T) : ToolExecutionState<T>()
    data class Error(val message: String, val exception: Throwable? = null) : ToolExecutionState<Nothing>()
}
