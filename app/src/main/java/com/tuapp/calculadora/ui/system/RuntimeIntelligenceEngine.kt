package com.tuapp.calculadora.ui.system

import com.tuapp.calculadora.ui.system.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class RuntimeIntelligenceEngine(
    private val telemetryManager: RuntimeTelemetryManager,
    private val externalScope: CoroutineScope
) {
    private val _signals = MutableSharedFlow<RuntimeSignal>(extraBufferCapacity = 64)
    val signals: SharedFlow<RuntimeSignal> = _signals.asSharedFlow()

    private val _adaptationHint = MutableStateFlow(
        RuntimeSignal.PerformanceHint(
            reduceBlur = false,
            reduceMotion = false,
            throttleTelemetryMs = 1000L,
            simplifyRendering = false
        )
    )
    val adaptationHint: StateFlow<RuntimeSignal.PerformanceHint> = _adaptationHint.asStateFlow()

    fun initialize() {
        externalScope.launch {
            telemetryManager.telemetryState.collect { state ->
                analyzeMetrics(state)
            }
        }
    }

    private suspend fun analyzeMetrics(state: HardwareState) {
        var needsReduceBlur = false
        var needsReduceMotion = false
        var targetedTelemetryDelay = 1000L
        var needsSimplifyRendering = false

        // 1. Análisis del Perfil Térmico Real
        when (state.thermal) {
            ThermalState.WARM -> {
                needsReduceBlur = true
                _signals.emit(RuntimeSignal.Warning("Elevación térmica de hardware. Degradando desenfoque.", RuntimeSignal.Warning.Level.LOW))
            }
            ThermalState.THROTTLING -> {
                needsReduceBlur = true
                needsReduceMotion = true
                targetedTelemetryDelay = 2000L
                _signals.emit(RuntimeSignal.Warning("Thermal Throttling detectado. Modulando ciclos de reloj internos.", RuntimeSignal.Warning.Level.HIGH))
            }
            ThermalState.CRITICAL -> {
                needsReduceBlur = true
                needsReduceMotion = true
                targetedTelemetryDelay = 3000L
                needsSimplifyRendering = true
                _signals.emit(RuntimeSignal.Warning("Peligro térmico en procesador. Forzando UI minimalista.", RuntimeSignal.Warning.Level.CRITICAL))
            }
            else -> {}
        }

        // 2. Análisis de Presión en la memoria RAM
        when (state.ram.pressure) {
            MemoryPressure.HIGH -> {
                targetedTelemetryDelay = maxOf(targetedTelemetryDelay, 2000L)
                _signals.emit(RuntimeSignal.Warning("Baja disponibilidad de RAM del sistema. Retrasando buffers secundarios.", RuntimeSignal.Warning.Level.MEDIUM))
            }
            MemoryPressure.CRITICAL -> {
                needsSimplifyRendering = true
                targetedTelemetryDelay = maxOf(targetedTelemetryDelay, 4000L)
                _signals.emit(RuntimeSignal.Warning("Advertencia severa de memoria (LowMemory. OOM inminente). Reduciendo hilos.", RuntimeSignal.Warning.Level.CRITICAL))
            }
            else -> {}
        }

        // 3. Estado Energético y Restricciones del Fabricante (Ahorro de batería)
        if (state.battery.isPowerSaverMode) {
            needsReduceMotion = true
            needsReduceBlur = true
            targetedTelemetryDelay = maxOf(targetedTelemetryDelay, 2500L)
        }

        // 4. Congestión Interna del Runtime Operativo
        if (state.runtime.eventBusQueueSize > 200 || state.runtime.avgPluginLatencyMs > 300L) {
            needsSimplifyRendering = true
            _signals.emit(RuntimeSignal.Warning("Sobrecarga en cola interna CoreEventBus. Mitigando pipelines gráficos.", RuntimeSignal.Warning.Level.HIGH))
        }

        // Aplicación e inyección atómica de la directiva de adaptación si hay cambios del sistema
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
            telemetryManager.updateSamplingInterval(targetedTelemetryDelay)
        }
    }
}
