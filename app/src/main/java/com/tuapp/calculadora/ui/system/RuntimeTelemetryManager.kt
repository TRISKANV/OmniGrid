package com.tuapp.calculadora.ui.system

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class LogLevel { INFO, WARN, CRITICAL, EXEC }

data class TelemetryEntry(
    val timestamp: Long = System.currentTimeMillis(),
    val tag: String,
    val message: String,
    val level: LogLevel
)

// Shims de compatibilidad para la UI antigua
typealias LogEntry = TelemetryEntry

object RuntimeTelemetryManager {
    private const val MAX_LOG_HISTORY = 150 
    
    private val _logs = MutableStateFlow<List<TelemetryEntry>>(emptyList())
    val logs: StateFlow<List<TelemetryEntry>> = _logs.asStateFlow()

    fun log(tag: String, message: String, level: LogLevel = LogLevel.INFO) {
        val entry = TelemetryEntry(tag = tag, message = message, level = level)
        
        _logs.update { currentLogs ->
            val newLogs = currentLogs + entry
            if (newLogs.size > MAX_LOG_HISTORY) newLogs.takeLast(MAX_LOG_HISTORY) else newLogs
        }
        
        CoreEventBus.publish(OmniEvent.TelemetryEmitted(entry))
    }
}
