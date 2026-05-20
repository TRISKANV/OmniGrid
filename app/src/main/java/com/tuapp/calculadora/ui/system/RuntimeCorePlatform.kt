package com.tuapp.calculadora.ui.system

import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

// ==========================================================================
// 1. RUNTIME EVENT ECOSYSTEM (Type-Safe, Ultra-Low-Latency Event Bus)
// ==========================================================================
sealed class RuntimeEvent {
    val timestamp: Long = System.currentTimeMillis()

    data class System(val msg: String, val level: LogLevel) : RuntimeEvent()
    data class Security(val tag: String, val event: String, val integrityAlert: Boolean) : RuntimeEvent()
    data class Transport(val Protocol: String, val target: String, val status: String) : RuntimeEvent()
    data class Hardware(val subsystem: String, val alert: String, val loadFactor: Float) : RuntimeEvent()
    data class Execution(val taskName: String, val durationMs: Long, val success: Boolean) : RuntimeEvent()
}

object RuntimeEventBus {
    private val _events = MutableSharedFlow<RuntimeEvent>(extraBufferCapacity = 64)
    val events: SharedFlow<RuntimeEvent> = _events.asSharedFlow()

    fun emit(event: RuntimeEvent) {
        _events.tryEmit(event)
        
        // Traducir de forma automática al subsistema clásico de logs para mantener consistencia visual
        when (event) {
            is RuntimeEvent.System -> RuntimeTelemetryManager.log("SYS", event.msg, event.level)
            is RuntimeEvent.Security -> RuntimeTelemetryManager.log("SEC", "[${event.tag}] ${event.event}", if (event.integrityAlert) LogLevel.CRITICAL else LogLevel.WARN)
            is RuntimeEvent.Transport -> RuntimeTelemetryManager.log("TRN", "${event.Protocol} -> ${event.target}: ${event.status}", LogLevel.EXEC)
            is RuntimeEvent.Hardware -> RuntimeTelemetryManager.log("HWD", "${event.subsystem.uppercase()}: ${event.alert}", LogLevel.WARN)
            is RuntimeEvent.Execution -> RuntimeTelemetryManager.log("EXE", "Task '${event.taskName}' completed in ${event.durationMs}ms", if (event.success) LogLevel.INFO else LogLevel.CRITICAL)
        }
    }
}

// ==========================================================================
// 2. RUNTIME SESSION SYSTEM (Control de Estado de la Plataforma)
// ==========================================================================
data class OperationalSession(
    val sessionId: String,
    val startTime: Long,
    val durationMs: Long,
    val totalOperations: Int,
    val operationalState: SessionState,
    val ActiveTransport: String
)

enum class SessionState { INITIALIZING, ACTIVE, INTERRUPTED, SECURE_LOCK }

object RuntimeSessionManager {
    private var currentSession: OperationalSession? = null
    private var operationCount = 0

    fun startSession(transport: String = "STANDALONE") {
        operationCount = 0
        currentSession = OperationalSession(
            sessionId = "OP-${System.currentTimeMillis() % 10000}",
            startTime = System.currentTimeMillis(),
            durationMs = 0L,
            totalOperations = 0,
            operationalState = SessionState.ACTIVE,
            ActiveTransport = transport
        )
        RuntimeEventBus.emit(RuntimeEvent.System("New operational session standard created: ${currentSession?.sessionId}", LogLevel.INFO))
    }

    fun registerActivity() {
        operationCount++
        RuntimeTelemetryManager.registerExecution()
    }

    fun getSessionMetrics(): OperationalSession {
        val session = currentSession ?: return OperationalSession("NULL", 0L, 0L, 0, SessionState.INITIALIZING, "NONE")
        return session.copy(
            durationMs = System.currentTimeMillis() - session.startTime,
            totalOperations = operationCount
        )
    }
}

// ==========================================================================
// 3. PLUGIN INFRASTRUCTURE CONTRACT & LAYOUT DEFINITIONS
// ==========================================================================
enum class ModuleSize { SMALL, WIDE }

data class PluginMetadata(
    val id: String,
    val name: String,
    val version: String,
    val priority: Int // Determina el ordenamiento automático dentro del Staggered Grid
)

interface RuntimePlugin {
    val metadata: PluginMetadata
    
    // Lifecycle Hooks (Para inicializar controladores, sockets o listeners de hardware)
    fun onPluginAttach()
    fun onPluginDetach()

    // UI Provider Rules
    @Composable
    fun RenderWidget(size: ModuleSize, systemMetrics: RuntimeMetrics)
}

// ==========================================================================
// 4. DEVICE & HARDWARE TELEMETRY SCHEMA
// ==========================================================================
data class DeviceHardwareState(
    val batteryLevel: Int,
    val thermalState: String,
    val usbConnected: Boolean,
    val otgDetected: Boolean,
    val bluetoothEnabled: Boolean,
    val networkLink: String
)
