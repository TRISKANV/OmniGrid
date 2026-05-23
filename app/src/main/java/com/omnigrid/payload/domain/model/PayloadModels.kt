package com.omnigrid.payload.domain.model

import java.util.UUID

data class Payload(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String = "",
    val script: String,
    val category: PayloadCategory = PayloadCategory.UNCATEGORIZED,
    val tags: List<String> = emptyList(),
    val isFavorite: Boolean = false,
    val workspaceId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val executionCount: Int = 0,
    val lastExecutedAt: Long? = null,
    val metadata: Map<String, String> = emptyMap()
)

enum class PayloadCategory(val label: String, val colorHex: String) {
    UNCATEGORIZED("Uncategorized", "#555566"),
    RECON("Recon", "#00FFAA"),
    PERSISTENCE("Persistence", "#FF4455"),
    EXFILTRATION("Exfiltration", "#FFAA00"),
    LATERAL_MOVEMENT("Lateral Movement", "#AA00FF"),
    EVASION("Evasion", "#0088FF"),
    CUSTOM("Custom", "#FFFFFF")
}

enum class ExecutionState {
    QUEUED, INITIALIZING, RUNNING, PAUSED, COMPLETING, COMPLETED, FAILED, CANCELLED, TIMEOUT
}

enum class TransportState {
    DISCONNECTED, CONNECTING, CONNECTED, TRANSMITTING, ERROR
}

data class PayloadSession(
    val sessionId: String = UUID.randomUUID().toString(),
    val payloadId: String,
    val payloadName: String,
    val startedAt: Long = System.currentTimeMillis(),
    val endedAt: Long? = null,
    val state: ExecutionState = ExecutionState.QUEUED,
    val totalActions: Int = 0,
    val completedActions: Int = 0,
    val failedActions: Int = 0,
    val progress: Float = 0f,
    val transportState: TransportState = TransportState.DISCONNECTED,
    val logs: List<SessionLog> = emptyList(),
    val warnings: List<String> = emptyList()
) {
    // ⚠️ ESTAS ERAN LAS PROPIEDADES QUE FALTABAN
    val duration: Long
        get() = (endedAt ?: System.currentTimeMillis()) - startedAt

    val isTerminal: Boolean
        get() = state in listOf(
            ExecutionState.COMPLETED,
            ExecutionState.FAILED,
            ExecutionState.CANCELLED,
            ExecutionState.TIMEOUT
        )
}

data class SessionLog(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val level: LogLevel,
    val tag: String,
    val message: String,
    val actionIndex: Int? = null
)

enum class LogLevel { DEBUG, INFO, WARN, ERROR, CRITICAL }

data class ExecutionMetrics(
    val queueWaitMs: Long = 0L,
    val initTimeMs: Long = 0L,
    val totalExecutionMs: Long = 0L,
    val avgActionLatencyMs: Long = 0L,
    val peakActionLatencyMs: Long = 0L,
    val retryCount: Int = 0,
    val bytesSent: Long = 0L
)
