package com.cyber.omnigrid.core.os.presentation

import android.content.Context
import android.os.Build
import android.os.PowerManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cyber.omnigrid.core.os.data.AndroidCapabilityManager
import com.cyber.omnigrid.core.os.domain.SystemCapabilities
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

enum class TransportState { IDLE, HANDSHAKE, CONNECTED, DISCONNECTED, ERROR }

data class LiveMetrics(
    val txLatencyMs: Long = 0,
    val rxLatencyMs: Long = 0,
    val handshakeDurationMs: Long = 0,
    val activeExecutors: Int = 0,
    val commandsInjected: Long = 0,
    val totalMemoryBytes: Long = 0,
    val freeMemoryBytes: Long = 0
)

data class SystemLogLine(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val tag: String,
    val message: String,
    val level: String = "INFO"
)

data class SystemHealthUiState(
    val capabilities: SystemCapabilities = SystemCapabilities(),
    val transportState: TransportState = TransportState.IDLE,
    val metrics: LiveMetrics = LiveMetrics(),
    val logs: List<SystemLogLine> = emptyList(),
    val isIgnoringBatteryOptimizations: Boolean = false,
    val deviceModel: String = Build.MODEL,
    val deviceManufacturer: String = Build.MANUFACTURER,
    val apiLevel: Int = Build.VERSION.SDK_INT
)

class SystemHealthViewModel(
    private val context: Context,
    private val capabilityManager: AndroidCapabilityManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SystemHealthUiState())
    val uiState: StateFlow<SystemHealthUiState> = _uiState.asStateFlow()

    init {
        refreshAll()
        runLiveTelemetrySimulation() // Simula telemetría en tiempo real hasta acoplar el motor síncrono externo
    }

    fun refreshAll() {
        capabilityManager.refreshCapabilities()
        
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        val isIgnoringBattery = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            powerManager?.isIgnoringBatteryOptimizations(context.packageName) ?: false
        } else {
            true
        }

        _uiState.update { currentState ->
            currentState.copy(
                capabilities = capabilityManager.capabilities.value,
                isIgnoringBatteryOptimizations = isIgnoringBattery
            )
        }
        logEvent("SYS", "Estado global de capacidades y restricciones OEM actualizado.")
    }

    fun logEvent(tag: String, message: String, level: String = "INFO") {
        val newLine = SystemLogLine(tag = tag, message = message, level = level)
        _uiState.update { state ->
            state.copy(logs = (state.logs + newLine).takeLast(50)) // Mantener últimas 50 líneas en memoria buffer
        }
    }

    private fun runLiveTelemetrySimulation() {
        viewModelScope.launch {
            logEvent("CORE", "Inicializando motores de observabilidad avanzada...")
            while (true) {
                delay(1000)
                val runtime = Runtime.getRuntime()
                val usedMem = runtime.totalMemory() - runtime.freeMemory()
                
                _uiState.update { state ->
                    state.copy(
                        metrics = state.metrics.copy(
                            txLatencyMs = (4..12).random().toLong(),
                            rxLatencyMs = (2..8).random().toLong(),
                            totalMemoryBytes = runtime.totalMemory(),
                            freeMemoryBytes = runtime.freeMemory()
                        )
                    )
                }
            }
        }
    }
}
