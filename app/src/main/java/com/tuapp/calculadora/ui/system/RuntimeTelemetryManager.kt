package com.tuapp.calculadora.ui.system

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class LogLevel { INFO, WARN, ERROR, CRITICAL }

data class TelemetryEntry(
    val timestamp: Long = System.currentTimeMillis(),
    val tag: String,
    val message: String,
    val level: LogLevel
)

object RuntimeTelemetryManager {
    // Mantenemos solo los últimos 150 eventos en memoria para garantizar OOM-safety
    private const val MAX_LOG_HISTORY = 150 
    
    private val _logs = MutableStateFlow<List<TelemetryEntry>>(emptyList())
    val logs: StateFlow<List<TelemetryEntry>> = _logs.asStateFlow()

    fun log(tag: String, message: String, level: LogLevel = LogLevel.INFO) {
        val entry = TelemetryEntry(tag = tag, message = message, level = level)
        
        _logs.update { currentLogs ->
            val newLogs = currentLogs + entry
            if (newLogs.size > MAX_LOG_HISTORY) newLogs.takeLast(MAX_LOG_HISTORY) else newLogs
        }
        
        // El log se transmite al instante a cualquier parte del OS que esté escuchando
        CoreEventBus.publish(OmniEvent.TelemetryEmitted(entry))
    }
}
