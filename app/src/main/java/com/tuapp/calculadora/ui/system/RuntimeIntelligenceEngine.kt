package com.tuapp.calculadora.ui.system

import android.content.Context
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tuapp.calculadora.ui.system.hal.MemoryProfile
import com.tuapp.calculadora.ui.system.hal.OmniDeviceHAL
import com.tuapp.calculadora.ui.system.hal.ThermalProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class SystemStressLevel { NOMINAL, ELEVATED, SEVERE, CRITICAL }

data class AdaptiveUIConfig(
    val blurRadius: Dp,
    val animationScale: Float,
    val ambientGlowOpacity: Float,
    val enableHaptics: Boolean,
    val renderComplexity: Int 
)

val LocalAdaptiveConfig = compositionLocalOf { 
    AdaptiveUIConfig(8.dp, 1.0f, 0.5f, true, 100) 
}

object RuntimeIntelligenceEngine {
    private val _stressLevel = MutableStateFlow(SystemStressLevel.NOMINAL)
    val stressLevel: StateFlow<SystemStressLevel> = _stressLevel.asStateFlow()

    private val _adaptiveConfig = MutableStateFlow(AdaptiveUIConfig(8.dp, 1.0f, 0.5f, true, 100))
    val adaptiveConfig: StateFlow<AdaptiveUIConfig> = _adaptiveConfig.asStateFlow()

    // Shim para el ModularDashboard antiguo
    private val _anomalies = MutableStateFlow<List<String>>(emptyList())
    val anomalies: StateFlow<List<String>> = _anomalies.asStateFlow()

    private var engineJob: Job? = null

    fun bootEngine(context: Context, scope: CoroutineScope = CoroutineScope(Dispatchers.Default)) {
        if (engineJob != null) return
        
        RuntimeTelemetryManager.log("ENGINE", "Living Runtime Booting: Hardware bindings established", LogLevel.INFO)
        
        engineJob = scope.launch {
            while (isActive) {
                val thermal = OmniDeviceHAL.getThermalProfile(context)
                val memory = OmniDeviceHAL.getMemoryProfile(context)
                analyzeSystemCycle(thermal, memory)
                delay(5000) 
            }
        }
    }

    // Shim para medir el throughput de eventos desde la plataforma base
    fun reportEventThroughput(throughput: Int) {
        if (throughput > 500) {
            RuntimeTelemetryManager.log("ENGINE", "High event throughput detected: $throughput ev/s", LogLevel.WARN)
        }
    }

    // Se cambia de private a público/internal para permitir llamadas externas heredadas
    fun analyzeSystemCycle(thermal: ThermalProfile, memory: MemoryProfile) {
        val oldStress = _stressLevel.value
        
        val newStress = when {
            thermal.isThrottling || memory.pressurePercent > 85 || memory.isLowMemory -> SystemStressLevel.CRITICAL
            thermal.cpuTempC > 38.0f || memory.pressurePercent > 75 -> SystemStressLevel.SEVERE
            thermal.cpuTempC > 35.0f || memory.pressurePercent > 65 -> SystemStressLevel.ELEVATED
            else -> SystemStressLevel.NOMINAL
        }

        val logLvl = if (newStress == SystemStressLevel.NOMINAL) LogLevel.INFO else LogLevel.WARN
        RuntimeTelemetryManager.log(
            "HAL_POLL",
            "RAM: ${memory.availableMB}MB free (${memory.pressurePercent}% loaded) | Temp: ${thermal.cpuTempC}°C | Bat: ${thermal.batteryLevel}%",
            logLvl
        )

        if (oldStress != newStress) {
            _stressLevel.value = newStress
            CoreEventBus.publish(OmniEvent.SystemStressChanged(oldStress, newStress))
            RuntimeTelemetryManager.log("INTELLIGENCE", "System stress shifted: ${oldStress.name} -> ${newStress.name}", if (newStress == SystemStressLevel.CRITICAL) LogLevel.CRITICAL else LogLevel.WARN)
            recalculateAdaptiveConfig(newStress)
        }
    }

    private fun recalculateAdaptiveConfig(stress: SystemStressLevel) {
        val config = when (stress) {
            SystemStressLevel.NOMINAL -> AdaptiveUIConfig(12.dp, 1.0f, 0.6f, true, 100)
            SystemStressLevel.ELEVATED -> AdaptiveUIConfig(6.dp, 0.8f, 0.4f, true, 80)
            SystemStressLevel.SEVERE -> AdaptiveUIConfig(0.dp, 0.5f, 0.1f, false, 50)
            SystemStressLevel.CRITICAL -> AdaptiveUIConfig(0.dp, 0.0f, 0.0f, false, 20)
        }
        _adaptiveConfig.update { config }
    }
}
