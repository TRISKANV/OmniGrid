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

    private var lastThermalState: ThermalState = ThermalState.COOL

    fun initialize() {
        externalScope.launch {
            telemetryManager.telemetryState.collect { state ->
                analyzeMetrics(state)
            }
        }
    }

    private fun mapThermalToLegacyStress(thermal: ThermalState): SystemStressLevel {
        return when (thermal) {
            ThermalState.COOL, ThermalState.MODERATE -> SystemStressLevel.NORMAL
            ThermalState.WARM -> SystemStressLevel.ELEVATED
            ThermalState.THROTTLING, ThermalState.CRITICAL -> SystemStressLevel.CRITICAL
        }
    }

    private suspend fun analyzeMetrics(state: HardwareState) {
        var needsReduceBlur = false
        var needsReduceMotion = false
        var targetedTelemetryDelay = 1000L
        var needsSimplifyRendering = false

        if (state.thermal != lastThermalState) {
            // 1. Notificación a la arquitectura nueva
            CoreEventBus.publish(OmniEvent.ThermalStateChanged(lastThermalState, state.thermal))
            
            // 2. Notificación puente a la UI antigua (SecureVault, etc.)
            val oldLegacy = mapThermalToLegacyStress(lastThermalState)
            val newLegacy = mapThermalToLegacyStress(state.thermal)
            if (oldLegacy != newLegacy) {
                CoreEventBus.publish(OmniEvent.SystemStressChanged(oldLegacy, newLegacy))
            }
            
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
                _signals.emit(RuntimeSignal.Warning(msg, RuntimeSignal.Warning.Level.HIGH))
                CoreEventBus.publish(OmniEvent.HardwareWarning(msg))
            }
            ThermalState.CRITICAL -> {
                needsReduceBlur = true
                needsReduceMotion = true
                targetedTelemetryDelay = 3000L
                needsSimplifyRendering = true
                val msg = "PELIGRO TÉRMICO EN PROCESADOR. Forzando modo UI minimalista."
                _signals.emit(RuntimeSignal.Warning(msg, RuntimeSignal.Warning.Level.CRITICAL))
                CoreEventBus.publish(OmniEvent.HardwareWarning(msg))
            }
            else -> {}
        }

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
                _signals.emit(RuntimeSignal.Warning(msg, RuntimeSignal.Warning.Level.CRITICAL))
                CoreEventBus.publish(OmniEvent.HardwareWarning(msg))
            }
            else -> {}
        }

        if (state.battery.isPowerSaverMode) {
            needsReduceMotion = true
            needsReduceBlur = true
            targetedTelemetryDelay = maxOf(targetedTelemetryDelay, 2500L)
        }

        if (state.runtime.eventBusQueueSize > 200 || state.runtime.avgPluginLatencyMs > 300L) {
            needsSimplifyRendering = true
            val msg = "Sobrecarga en cola interna CoreEventBus. Mitigando pipelines gráficos."
            _signals.emit(RuntimeSignal.Warning(msg, RuntimeSignal.Warning.Level.HIGH))
            CoreEventBus.publish(OmniEvent.HardwareWarning(msg))
        }

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
