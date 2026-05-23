package com.omnigrid.payload.runtime.events

import com.omnigrid.payload.domain.model.ExecutionMetrics
import com.omnigrid.payload.domain.model.ExecutionState
import com.omnigrid.payload.domain.model.TransportState

sealed class PayloadRuntimeEvent {
    abstract val sessionId: String
    abstract val timestamp: Long

    data class SessionStateChanged(
        override val sessionId: String,
        val state: ExecutionState,
        override val timestamp: Long = System.currentTimeMillis()
    ) : PayloadRuntimeEvent()

    data class SessionCompleted(
        override val sessionId: String,
        val metrics: ExecutionMetrics,
        val completedActions: Int,
        val failedActions: Int,
        override val timestamp: Long = System.currentTimeMillis()
    ) : PayloadRuntimeEvent()

    data class SessionFailed(
        override val sessionId: String,
        val reason: String,
        override val timestamp: Long = System.currentTimeMillis()
    ) : PayloadRuntimeEvent()

    data class ParseCompleted(
        override val sessionId: String,
        val actionCount: Int,
        val warnings: List<String>,
        override val timestamp: Long = System.currentTimeMillis()
    ) : PayloadRuntimeEvent()

    data class ActionStarted(
        override val sessionId: String,
        val actionIndex: Int,
        val actionType: String,
        override val timestamp: Long = System.currentTimeMillis()
    ) : PayloadRuntimeEvent()

    data class ActionCompleted(
        override val sessionId: String,
        val actionIndex: Int,
        val latencyMs: Long,
        val completed: Int,
        val total: Int,
        override val timestamp: Long = System.currentTimeMillis()
    ) : PayloadRuntimeEvent()

    data class ActionFailed(
        override val sessionId: String,
        val actionIndex: Int,
        val reason: String,
        val isRecoverable: Boolean,
        override val timestamp: Long = System.currentTimeMillis()
    ) : PayloadRuntimeEvent()

    data class TransportStateChanged(
        override val sessionId: String,
        val state: TransportState,
        override val timestamp: Long = System.currentTimeMillis()
    ) : PayloadRuntimeEvent()

    data class Warning(
        override val sessionId: String,
        val message: String,
        override val timestamp: Long = System.currentTimeMillis()
    ) : PayloadRuntimeEvent()
}

// Extensiones para Logs
fun PayloadRuntimeEvent.toLogLevel(): com.omnigrid.payload.domain.model.LogLevel =
    when (this) {
        is PayloadRuntimeEvent.SessionFailed -> com.omnigrid.payload.domain.model.LogLevel.CRITICAL
        is PayloadRuntimeEvent.ActionFailed -> com.omnigrid.payload.domain.model.LogLevel.ERROR
        is PayloadRuntimeEvent.Warning -> com.omnigrid.payload.domain.model.LogLevel.WARN
        is PayloadRuntimeEvent.SessionCompleted -> com.omnigrid.payload.domain.model.LogLevel.INFO
        else -> com.omnigrid.payload.domain.model.LogLevel.DEBUG
    }

fun PayloadRuntimeEvent.toLogMessage(): String = when (this) {
    is PayloadRuntimeEvent.SessionStateChanged -> "State → ${state.name}"
    is PayloadRuntimeEvent.SessionCompleted -> "✓ Completed — $completedActions OK / $failedActions FAIL — ${metrics.totalExecutionMs}ms"
    is PayloadRuntimeEvent.SessionFailed -> "✗ Failed: $reason"
    is PayloadRuntimeEvent.ParseCompleted -> "Parse OK — $actionCount actions (${warnings.size} warnings)"
    is PayloadRuntimeEvent.ActionStarted -> "[$actionIndex] → $actionType"
    is PayloadRuntimeEvent.ActionCompleted -> "[$actionIndex] ✓ ${latencyMs}ms — $completed/$total"
    is PayloadRuntimeEvent.ActionFailed -> "[$actionIndex] ✗ $reason"
    is PayloadRuntimeEvent.TransportStateChanged -> "Transport → ${state.name}"
    is PayloadRuntimeEvent.Warning -> "⚠ $message"
}
