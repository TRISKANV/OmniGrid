package com.tuapp.calculadora.ui.system

import android.os.SystemClock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

// --- ESTRUCTURA DE DATOS DE RUNTIME ---
data class TelemetryLog(
    val timestamp: String,
    val tag: String,
    val message: String,
    val level: LogLevel = LogLevel.INFO
)

enum class LogLevel { INFO, WARN, CRITICAL, EXEC }

data class RuntimeMetrics(
    val memoryUsageMb: Double,
    val activeCoroutines: Int,
    val queueSize: Int,
    val totalExecutions: Long,
    val transportState: String
)

// --- OMNI RUNTIME EVENT BUS ---
object RuntimeTelemetryManager {
    
    private const val MAX_LOG_BUFFER = 50

    // Búfer circular de logs persistente en memoria para evitar pérdida de eventos con UI cerrada
    private val _logHistory = MutableStateFlow<List<TelemetryLog>>(emptyList())
    val logHistory: StateFlow<List<TelemetryLog>> = _logHistory.asStateFlow()

    // Métricas globales del sistema
    private val _metrics = MutableStateFlow(
        RuntimeMetrics(
            memoryUsageMb = 0.0,
            activeCoroutines = 0,
            queueSize = 0,
            totalExecutions = 0L,
            transportState = "STANDBY"
        )
    )
    val metrics: StateFlow<RuntimeMetrics> = _metrics.asStateFlow()

    // Contadores atómicos internos de alta concurrencia
    private val activeCoroutinesCount = AtomicInteger(0)
    private val currentQueueSize = AtomicInteger(0)
    private val executionCounter = AtomicLong(0L)
    private var currentTransportState = "STANDBY"

    init {
        // Inicializar con huella de arranque limpia
        log("CORE", "OmniGrid Engine initialization sequence complete.", LogLevel.INFO)
        updateSystemMemory()
    }

    // API pública de logging para CryptoManager, BluetoothHidExecutor, etc.
    fun log(tag: String, message: String, level: LogLevel = LogLevel.INFO) {
        val currentLogs = _logHistory.value
        val timestamp = formatCurrentTime()
        val newLog = TelemetryLog(timestamp, tag, message, level)
        
        // Mantener tamaño de buffer controlado para evitar memory leaks
        _logHistory.value = (currentLogs + newLog).takeLast(MAX_LOG_BUFFER)
    }

    // --- INTERFACES DE CONTROL DE TELEMETRÍA ---
    fun incrementCoroutines() {
        activeCoroutinesCount.incrementAndGet()
        pushMetrics()
    }

    fun decrementCoroutines() {
        if (activeCoroutinesCount.get() > 0) activeCoroutinesCount.decrementAndGet()
        pushMetrics()
    }

    fun setQueueSize(size: Int) {
        currentQueueSize.set(size)
        pushMetrics()
    }

    fun registerExecution() {
        executionCounter.incrementAndGet()
        pushMetrics()
    }

    fun setTransportState(state: String) {
        currentTransportState = state
        pushMetrics()
    }

    /**
     * Sincroniza las métricas del sistema leyendo el entorno nativo de la máquina virtual.
     * Debe llamarse periódicamente desde el bucle del Dashboard o ForegroundService.
     */
    fun updateSystemMemory() {
        val runtime = Runtime.getRuntime()
        val usedMemoryBytes = runtime.totalMemory() - runtime.freeMemory()
        val usedMemoryMb = usedMemoryBytes.toDouble() / (1024.0 * 1024.0)
        
        _metrics.value = _metrics.value.copy(
            memoryUsageMb = usedMemoryMb,
            activeCoroutines = activeCoroutinesCount.get(),
            queueSize = currentQueueSize.get(),
            totalExecutions = executionCounter.get(),
            transportState = currentTransportState
        )
    }

    private fun pushMetrics() {
        _metrics.value = _metrics.value.copy(
            activeCoroutines = activeCoroutinesCount.get(),
            queueSize = currentQueueSize.get(),
            totalExecutions = executionCounter.get(),
            transportState = currentTransportState
        )
    }

    private fun formatCurrentTime(): String {
        val millis = System.currentTimeMillis()
        val seconds = (millis / 1000) % 60
        val minutes = (millis / (1000 * 60)) % 60
        val hours = (millis / (1000 * 60 * 60)) % 24
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }
}
