package com.omnigrid.payload.runtime.events

import com.omnigrid.payload.domain.model.ExecutionState

class PayloadEventDispatcher(
    private val coreEventBus: OmniRuntimeEventBus,
    private val telemetry: OmniTelemetryBridge,
    private val timeline: OmniTimelineBridge
) {
    suspend fun dispatch(event: PayloadRuntimeEvent) {
        when (event) {
            is PayloadRuntimeEvent.SessionStateChanged -> {
                coreEventBus.emit(
                    OmniRuntimeEvent.ModuleEvent(
                        module = "PayloadManager",
                        type = "SESSION_STATE",
                        payload = mapOf("sessionId" to event.sessionId, "state" to event.state.name)
                    )
                )
                if (event.state in listOf(ExecutionState.RUNNING, ExecutionState.COMPLETED, ExecutionState.FAILED)) {
                    timeline.record("payload.manager", "execution.${event.state.name.lowercase()}", "Payload Session → ${event.state.name}", System.currentTimeMillis())
                }
            }
            is PayloadRuntimeEvent.SessionCompleted -> {
                coreEventBus.emit(
                    OmniRuntimeEvent.ModuleEvent(
                        module = "PayloadManager",
                        type = "SESSION_COMPLETED",
                        payload = mapOf(
                            "sessionId" to event.sessionId,
                            "durationMs" to event.metrics.totalExecutionMs.toString(),
                            "completedActions" to event.completedActions.toString(),
                            "failedActions" to event.failedActions.toString()
                        )
                    )
                )
                telemetry.record(TelemetryMetric("payload.manager", "session.duration_ms", event.metrics.totalExecutionMs.toDouble(), tags = mapOf("sessionId" to event.sessionId)))
                telemetry.record(TelemetryMetric("payload.manager", "session.success_rate", if (event.failedActions == 0) 1.0 else event.completedActions.toDouble() / (event.completedActions + event.failedActions).toDouble()))
                timeline.record("payload.manager", "execution.completed", "Session completed (${event.completedActions} actions)", event.timestamp)
            }
            is PayloadRuntimeEvent.SessionFailed -> {
                coreEventBus.emit(
                    OmniRuntimeEvent.ModuleEvent("PayloadManager", "SESSION_FAILED", mapOf("sessionId" to event.sessionId, "reason" to event.reason))
                )
                telemetry.record(TelemetryMetric("payload.manager", "session.failure", 1.0, tags = mapOf("sessionId" to event.sessionId, "reason" to event.reason.take(100))))
            }
            is PayloadRuntimeEvent.Warning -> {
                telemetry.record(TelemetryMetric("payload.manager", "session.warning", 1.0, tags = mapOf("sessionId" to event.sessionId)))
            }
            else -> { /* Eventos de bajo nivel: solo para UI local, no ensucian el OS */ }
        }
    }
}

// ── Interfaces Puente (Aisla el módulo del Kernel real por ahora) ──
interface OmniRuntimeEventBus { suspend fun emit(event: OmniRuntimeEvent) }
interface OmniTelemetryBridge { fun record(metric: TelemetryMetric) }
interface OmniTimelineBridge { fun record(moduleId: String, eventType: String, label: String, timestamp: Long) }

sealed class OmniRuntimeEvent {
    data class ModuleEvent(val module: String, val type: String, val payload: Map<String, String> = emptyMap(), val timestamp: Long = System.currentTimeMillis()) : OmniRuntimeEvent()
}
data class TelemetryMetric(val module: String, val key: String, val value: Double, val timestamp: Long = System.currentTimeMillis(), val tags: Map<String, String> = emptyMap())
