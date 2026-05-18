package com.cyber.omnigrid.feature.automation.domain.engine

/**
 * Eventos reactivos que el Engine emite hacia la UI.
 * Esto garantiza la experiencia "cinematográfica" y en vivo.
 */
sealed interface ExecutionEvent {
    data class StatusChange(val status: ExecutionStatus) : ExecutionEvent
    data class Log(val level: LogLevel, val message: String, val timestamp: Long = System.currentTimeMillis()) : ExecutionEvent
    data class Progress(val currentStep: Int, val totalSteps: Int) : ExecutionEvent
}

enum class ExecutionStatus { IDLE, CONNECTING, RUNNING, SUCCESS, ERROR, CANCELLED }
enum class LogLevel { INFO, WARN, ERROR, SUCCESS, PAYLOAD_STEP }
