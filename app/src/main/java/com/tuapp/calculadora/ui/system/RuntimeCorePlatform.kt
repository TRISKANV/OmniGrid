package com.tuapp.calculadora.ui.system

import androidx.compose.runtime.Composable
import com.tuapp.calculadora.ui.system.hal.OmniDeviceHAL
import com.tuapp.calculadora.ui.system.hal.RuntimeIntelligenceEngine
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

// ==========================================================================
// 1. RUNTIME EVENT BUS & EVENT DEFINITIONS
// Se combinan los eventos Legacy (para no romper la app) y los del nuevo OS
// ==========================================================================
enum class LogLevel { INFO, WARN, CRITICAL, EXEC }

sealed class RuntimeEvent {
    val timestamp: Long = java.lang.System.currentTimeMillis()

    // --- Legacy Events (Mantenidos para retrocompatibilidad) ---
    data class System(val msg: String, val level: LogLevel) : RuntimeEvent()
    data class Security(val tag: String, val event: String, val integrityAlert: Boolean) : RuntimeEvent()
    data class Transport(val protocol: String, val target: String, val status: String) : RuntimeEvent()
    data class Hardware(val subsystem: String, val alert: String, val loadFactor: Float) : RuntimeEvent()
    data class Execution(val taskName: String, val durationMs: Long, val success: Boolean) : RuntimeEvent()
    
    // --- Omni OS Events (Nueva Arquitectura) ---
    data class Log(val tag: String, val msg: String) : RuntimeEvent()
    data class SecurityAlert(val code: String, val description: String) : RuntimeEvent()
}

object OmniEventBus {
    private val _events = MutableSharedFlow<RuntimeEvent>(extraBufferCapacity = 128)
    val events: SharedFlow<RuntimeEvent> = _events.asSharedFlow()

    fun dispatch(event: RuntimeEvent) {
        _events.tryEmit(event)
        RuntimeIntelligenceEngine.reportEventThroughput(1)
    }
}

// ==========================================================================
// 2. OPERATIONAL SESSION ENGINE (El nuevo motor)
// ==========================================================================
enum class SessionStatus { INITIATING, NOMINAL, DEGRADED, TERMINATED }

data class TacticalSession(
    val sessionId: String,
    val bootTimestamp: Long,
    val activeTransport: String,
    var status: SessionStatus,
    var peakMemoryUsageMb: Long,
    var criticalAnomalies: Int,
    var operationsExecuted: Long
) {
    val uptimeMs: Long get() = java.lang.System.currentTimeMillis() - bootTimestamp
}

object SessionOrchestrator {
    private var activeSession: TacticalSession? = null

    fun bootstrapSession(transport: String = "CORE_MESH") {
        activeSession = TacticalSession(
            sessionId = "OP-SYS-${java.lang.System.currentTimeMillis() % 10000}",
            bootTimestamp = java.lang.System.currentTimeMillis(),
            activeTransport = transport,
            status = SessionStatus.INITIATING,
            peakMemoryUsageMb = 0L,
            criticalAnomalies = 0,
            operationsExecuted = 0L
        )
        OmniEventBus.dispatch(RuntimeEvent.Log("BOOT", "Session ${activeSession?.sessionId} initialized over $transport"))
        activeSession?.status = SessionStatus.NOMINAL
    }

    fun tick() {
        val session = activeSession ?: return
        val mem = OmniDeviceHAL.fetchMemoryProfile()
        if (mem.totalMb - mem.availableMb > session.peakMemoryUsageMb) {
            session.peakMemoryUsageMb = mem.totalMb - mem.availableMb
        }
        RuntimeIntelligenceEngine.analyzeSystemCycle(OmniDeviceHAL.fetchThermalProfile(), mem)
    }

    fun getSessionManifest(): TacticalSession? = activeSession
}

// ==========================================================================
// 3. LEGACY ADAPTERS & BRIDGES (Evita el error <Error module> en KAPT)
// Estos objetos actúan como puentes hacia la nueva arquitectura.
// ==========================================================================
object RuntimeEventBus {
    fun emit(event: RuntimeEvent) {
        OmniEventBus.dispatch(event) // Redirige el tráfico viejo al nuevo motor
    }
}

enum class SessionState { INITIALIZING, ACTIVE, INTERRUPTED, SECURE_LOCK }
    
data class OperationalSession(
    val sessionId: String, val startTime: Long, val durationMs: Long,
    val totalOperations: Int, val operationalState: SessionState, val activeTransport: String
)

object RuntimeSessionManager {
    fun startSession(transport: String = "STANDALONE") = SessionOrchestrator.bootstrapSession(transport)
    fun registerActivity() {}
    fun getSessionMetrics(): OperationalSession {
        val osSession = SessionOrchestrator.getSessionManifest()
        return OperationalSession(
            sessionId = osSession?.sessionId ?: "NULL",
            startTime = osSession?.bootTimestamp ?: 0L,
            durationMs = osSession?.uptimeMs ?: 0L,
            totalOperations = osSession?.operationsExecuted?.toInt() ?: 0,
            operationalState = SessionState.ACTIVE,
            activeTransport = osSession?.activeTransport ?: "NONE"
        )
    }
}

enum class ModuleSize { SMALL, WIDE }

data class PluginMetadata(val id: String, val name: String, val version: String, val priority: Int)

// Asumiendo que RuntimeMetrics existe en otro archivo del paquete
interface RuntimePlugin {
    val metadata: PluginMetadata
    fun onPluginAttach()
    fun onPluginDetach()
    // Nota: El sistema legacy requiere este contrato.
}

data class DeviceHardwareState(
    val batteryLevel: Int, val thermalState: String, val usbConnected: Boolean,
    val otgDetected: Boolean, val bluetoothEnabled: Boolean, val networkLink: String
)
