package com.tuapp.calculadora.ui.system

import com.tuapp.calculadora.core.*
import com.tuapp.calculadora.core.CoreEventBus
import com.tuapp.calculadora.core.OmniEvent
import com.tuapp.calculadora.ui.system.model.*
import com.tuapp.calculadora.ui.system.CoreEventBus
import com.tuapp.calculadora.ui.system.OmniEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

object RuntimeIntelligenceEngine {
    private var externalScopeRef: CoroutineScope? = null

    private val _signals = MutableSharedFlow<RuntimeSignal>(extraBufferCapacity = 64)
    val signals: SharedFlow<RuntimeSignal> = _signals.asSharedFlow()

    private val _anomalies = MutableStateFlow<List<String>>(emptyList())
    val anomalies: StateFlow<List<String>> = _anomalies.asStateFlow()

    private val _adaptationHint = MutableStateFlow(
        RuntimeSignal.PerformanceHint(
            reduceBlur = false,
            reduceMotion = false,
            throttleTelemetryMs = 1000L,
            simplifyRendering = false
        )
    )
    val adaptationHint: StateFlow<RuntimeSignal.PerformanceHint> = _adaptationHint.asStateFlow()

    private var lastThermalState: ThermalState = ThermalState.COOL

    fun initialize(scope: CoroutineScope) {
        this.externalScopeRef = scope
        scope.launch {
            RuntimeTelemetryManager.telemetryState.collect { state ->
                analyzeMetrics(state)
            }
        }
    }

    fun analyzeSystemCycle() {
        // Ejecución preventiva por tick de la CPU/plataforma core real
        RuntimeTelemetryManager.reportEventThroughput(1)
    }

    private suspend fun analyzeMetrics(state: HardwareState) {
        var needsReduceBlur = false
        var needsReduceMotion = false
        var targetedTelemetryDelay = 1000L
        var needsSimplifyRendering = false
        val currentAnomalies = mutableListOf<String>()

        // Gestión reactiva del estado térmico real de la CPU
        if (state.thermal != lastThermalState) {
            CoreEventBus.publish(OmniEvent.ThermalStateChanged(lastThermalState, state.thermal))
            lastThermalState = state.thermal
        }

        when (state.thermal) {
            ThermalState.WARM -> {
                needsReduceBlur = true
                val msg = "Elevación térmica de hardware. Degradando desenfoque superficial."
                _signals.emit(RuntimeSignal.Warning(msg, RuntimeSignal.Warning.Level.LOW))
                CoreEventBus.publish(OmniEvent.HardwareWarning(msg))
            }
            ThermalState.THROTTLING -> {
                needsReduceBlur = true
                needsReduceMotion = true
                targetedTelemetryDelay = 2000L
                val msg = "Thermal Throttling severo. Modulando ciclos de reloj internos."
                currentAnomalies.add("THERMAL_THROTTLING")
                _signals.emit(RuntimeSignal.Warning(msg, RuntimeSignal.Warning.Level.HIGH))
                CoreEventBus.publish(OmniEvent.HardwareWarning(msg))
            }
            ThermalState.CRITICAL -> {
                needsReduceBlur = true
                needsReduceMotion = true
                targetedTelemetryDelay = 3000L
                needsSimplifyRendering = true
                val msg = "PELIGRO TÉRMICO EN PROCESADOR. Forzando modo UI minimalista."
                currentAnomalies.add("CRITICAL_THERMAL_OVERLOAD")
                _signals.emit(RuntimeSignal.Warning(msg, RuntimeSignal.Warning.Level.CRITICAL))
                CoreEventBus.publish(OmniEvent.HardwareWarning(msg))
            }
            else -> {}
        }

        // Análisis de presión sobre la memoria RAM física
        when (state.ram.pressure) {
            MemoryPressure.HIGH -> {
                targetedTelemetryDelay = maxOf(targetedTelemetryDelay, 2000L)
                val msg = "Baja disponibilidad de RAM del sistema. Retrasando buffers secundarios."
                _signals.emit(RuntimeSignal.Warning(msg, RuntimeSignal.Warning.Level.MEDIUM))
                CoreEventBus.publish(OmniEvent.HardwareWarning(msg))
            }
            MemoryPressure.CRITICAL -> {
                needsSimplifyRendering = true
                targetedTelemetryDelay = maxOf(targetedTelemetryDelay, 4000L)
                val msg = "Advertencia severa de memoria (LowMemory OS). OOM Inminente."
                currentAnomalies.add("LOW_MEMORY_CRITICAL")
                _signals.emit(RuntimeSignal.Warning(msg, RuntimeSignal.Warning.Level.CRITICAL))
                CoreEventBus.publish(OmniEvent.HardwareWarning(msg))
            }
            else -> {}
        }

        // Adaptación automática por modo Ahorro de Batería del dispositivo Android
        if (state.battery.isPowerSaverMode) {
            needsReduceMotion = true
            needsReduceBlur = true
            targetedTelemetryDelay = maxOf(targetedTelemetryDelay, 2500L)
        }

        // Control de congestión del EventBus asíncrono
        if (state.runtime.eventBusQueueSize > 200 || state.runtime.avgPluginLatencyMs > 300L) {
            needsSimplifyRendering = true
            val msg = "Sobrecarga en cola interna CoreEventBus. Mitigando pipelines gráficos."
            currentAnomalies.add("RUNTIME_CONGESTION")
            _signals.emit(RuntimeSignal.Warning(msg, RuntimeSignal.Warning.Level.HIGH))
            CoreEventBus.publish(OmniEvent.HardwareWarning(msg))
        }

        _anomalies.value = currentAnomalies

        // Aplicación del Hint de rendimiento optimizado si hubo mutaciones en este ciclo
        val currentHint = _adaptationHint.value
        if (currentHint.reduceBlur != needsReduceBlur ||
            currentHint.reduceMotion != needsReduceMotion ||
            currentHint.throttleTelemetryMs != targetedTelemetryDelay ||
            currentHint.simplifyRendering != needsSimplifyRendering
        ) {
            _adaptationHint.value = RuntimeSignal.PerformanceHint(
                reduceBlur = needsReduceBlur,
                reduceMotion = needsReduceMotion,
                throttleTelemetryMs = targetedTelemetryDelay,
                simplifyRendering = needsSimplifyRendering
            )
            RuntimeTelemetryManager.updateSamplingInterval(targetedTelemetryDelay)
        }
    }
}
