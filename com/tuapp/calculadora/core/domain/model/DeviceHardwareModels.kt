// Archivo: com/tuapp/calculadora/core/domain/model/DeviceHardwareModels.kt
package com.tuapp.calculadora.core.domain.model

enum class ThermalState { COOL, MODERATE, WARM, THROTTLING, CRITICAL }
enum class MemoryPressure { LOW, MEDIUM, HIGH, CRITICAL }
enum class NetworkQuality { EXCELLENT, GOOD, POOR, DISCONNECTED }

data class CpuMetrics(
    val coreCount: Int,
    val usagePercentage: Float,
    val activeThreads: Int
)

data class RamMetrics(
    val totalBytes: Long,
    val availableBytes: Long,
    val runtimeUsedBytes: Long,
    val pressure: MemoryPressure
)

data class BatteryMetrics(
    val percentage: Int,
    val isCharging: Boolean,
    val voltageMv: Int,
    val temperatureC: Float,
    val isPowerSaverMode: Boolean
)

data class NetworkMetrics(
    val quality: NetworkQuality,
    val transportType: String,
    val linkDownstreamBandwidthKbps: Int
)

data class RuntimeInternalMetrics(
    val activeCoroutines: Int,
    val eventBusQueueSize: Int,
    val avgPluginLatencyMs: Long,
    val processingThroughput: Int
)

data class DeviceProfile(
    val oem: String,
    val model: String,
    val apiLevel: Int,
    val isBackgroundRestricted: Boolean
)

data class HardwareState(
    val cpu: CpuMetrics,
    val ram: RamMetrics,
    val battery: BatteryMetrics,
    val network: NetworkMetrics,
    val runtime: RuntimeInternalMetrics,
    val profile: DeviceProfile,
    val thermal: ThermalState,
    val timestamp: Long
)

sealed interface RuntimeSignal {
    data class Warning(val message: String, val level: Level) : RuntimeSignal {
        enum class Level { LOW, MEDIUM, HIGH, CRITICAL }
    }
    data class PerformanceHint(
        val reduceBlur: Boolean,
        val reduceMotion: Boolean,
        val throttleTelemetryMs: Long,
        val simplifyRendering: Boolean
    ) : RuntimeSignal
}
