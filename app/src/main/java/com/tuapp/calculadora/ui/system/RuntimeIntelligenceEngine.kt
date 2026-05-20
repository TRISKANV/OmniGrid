package com.tuapp.calculadora.ui.system

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tuapp.calculadora.ui.system.hal.OmniDeviceHAL
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class SystemStressLevel { NOMINAL, ELEVATED, SEVERE, CRITICAL }

// La configuración que la UI leerá para degradarse o mejorarse sutilmente
data class AdaptiveUIConfig(
    val blurRadius: Dp,
    val animationScale: Float, // 1.0f = normal, 0.0f = sin animaciones
    val ambientGlowOpacity: Float,
    val enableHaptics: Boolean,
    val renderComplexity: Int // 100 = full, 50 = simplified
)

val LocalAdaptiveConfig = compositionLocalOf { 
    AdaptiveUIConfig(8.dp, 1.0f, 0.5f, true, 100) 
}

object RuntimeIntelligenceEngine {
    private val _stressLevel = MutableStateFlow(SystemStressLevel.NOMINAL)
    val stressLevel: StateFlow<SystemStressLevel> = _stressLevel.asStateFlow()

    private val _adaptiveConfig = MutableStateFlow(LocalAdaptiveConfig.defaultFactory())
    val adaptiveConfig: StateFlow<AdaptiveUIConfig> = _adaptiveConfig.asStateFlow()

    // Este tick es llamado por el SessionOrchestrator cada segundo
    fun analyzeSystemCycle(thermal: OmniDeviceHAL.ThermalProfile, memory: OmniDeviceHAL.MemoryProfile) {
        val newStress = when {
            thermal.isThrottling || memory.pressurePercent > 90 -> SystemStressLevel.CRITICAL
            thermal.cpuTempC > 75 || memory.pressurePercent > 80 -> SystemStressLevel.SEVERE
            thermal.cpuTempC > 60 || memory.pressurePercent > 65 -> SystemStressLevel.ELEVATED
            else -> SystemStressLevel.NOMINAL
        }

        if (_stressLevel.value != newStress) {
            _stressLevel.value = newStress
            RuntimeTelemetryManager.log("INTELLIGENCE", "System stress shifted to ${newStress.name}", LogLevel.WARN)
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
