package com.cyber.omnigrid.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "execution_logs")
data class ExecutionLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: String,                  // FK a ExecutionSession
    val timestamp: Long = System.currentTimeMillis(),
    val logType: LogLevel,                  // INFO, WARN, ERROR, PAYLOAD_STEP
    val message: String                     // Ej: "[Rucky] Inyectando línea 45..."
)

enum class LogLevel { INFO, WARN, ERROR, PAYLOAD_STEP, METRIC }
