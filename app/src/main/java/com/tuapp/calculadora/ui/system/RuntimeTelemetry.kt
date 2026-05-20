package com.tuapp.calculadora.ui.system

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

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
    val transportState: String,
    val hardwareState: DeviceHardwareState,
    val activeSessionId: String,
    val sessionDurationFormatted: String
)

object RuntimeTelemetryManager {
    
    private const val MAX_LOG_BUFFER = 60

    private val _logHistory = MutableStateFlow<List<TelemetryLog>>(emptyList())
    val logHistory: StateFlow<List<TelemetryLog>> = _logHistory.asStateFlow()

    // Estado inicial acoplado con variables simuladas del entorno real de hardware
    private val _metrics = MutableStateFlow(
        RuntimeMetrics(
            memoryUsageMb = 0.0,
            activeCoroutines = 0,
            queueSize = 0,
            totalExecutions = 0L,
            transportState = "CORE_NOMINAL",
            hardwareState = DeviceHardwareState(
                batteryLevel = 100,
                thermalState = "NOMINAL",
                usbConnected = false,
                otgDetected = false,
                bluetoothEnabled = true,
                networkLink = "ENCRYPTED_MESH"
            ),
            activeSessionId = "OFFLINE",
            sessionDurationFormatted = "00:00"
        )
    )
    val metrics: StateFlow<RuntimeMetrics> = _metrics.asStateFlow()

    private val activeCoroutinesCount = AtomicInteger(0)
    private val currentQueueSize = AtomicInteger(0)
    private val executionCounter = AtomicLong(0L)
    private var currentTransportState = "CORE_NOMINAL"

    init {
        log("CORE", "OmniGrid Platform Stack fully operational.", LogLevel.INFO)
        updateSystemStateDirect()
    }

    fun log(tag: String, message: String, level: LogLevel = LogLevel.INFO) {
        val currentLogs = _logHistory.value
        val timestamp = formatCurrentTime()
        val newLog = TelemetryLog(timestamp, tag, message, level)
        _logHistory.value = (currentLogs + newLog).takeLast(MAX_LOG_BUFFER)
    }

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

    fun updateSystemStateDirect() {
        val runtime = Runtime.getRuntime()
        val usedMemoryBytes = runtime.totalMemory() - runtime.freeMemory()
        val usedMemoryMb = usedMemoryBytes.toDouble() / (1024.0 * 1024.0)
        
        val session = RuntimeSessionManager.getSessionMetrics()
        
        // Simulación controlada y segura de fluctuación de telemetría de hardware
        // para alimentar los gráficos reactivos de la UI sin bloquear hilos.
        val mockBattery = (78..82).random()
        val formattedDuration = String.format("%02d:%02d", (session.durationMs / 60000) % 60, (session.durationMs / 1000) % 60)

        _metrics.value = _metrics.value.copy(
            memoryUsageMb = usedMemoryMb,
            activeCoroutines = activeCoroutinesCount.get(),
            queueSize = currentQueueSize.get(),
            totalExecutions = executionCounter.get(),
            transportState = currentTransportState,
            activeSessionId = session.sessionId,
            sessionDurationFormatted = formattedDuration,
            hardwareState = _metrics.value.hardwareState.copy(
                batteryLevel = mockBattery,
                thermalState = if (usedMemoryMb > 120) "ELEVATED" else "NOMINAL"
            )
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
