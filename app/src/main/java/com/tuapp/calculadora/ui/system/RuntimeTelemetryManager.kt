package com.tuapp.calculadora.ui.system

import android.app.ActivityManager
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build
import android.os.PowerManager
import com.tuapp.calculadora.ui.system.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.RandomAccessFile
import java.util.concurrent.atomic.AtomicInteger

class RuntimeTelemetryManager(
    private val context: Context,
    private val externalScope: CoroutineScope
) {
    private val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager

    private val coroutineCounter = AtomicInteger(0)
    private val eventBusQueueCounter = AtomicInteger(0)
    private val totalLatencyAccumulator = AtomicInteger(0)
    private val pluginExecutionCount = AtomicInteger(0)

    private val _telemetryState = MutableStateFlow(createInitialState())
    val telemetryState: StateFlow<HardwareState> = _telemetryState.asStateFlow()

    private var samplingJob: Job? = null
    private var sampleIntervalMs = 1000L

    private var lastCpuTime = 0L
    private var lastAppCpuTime = 0L

    fun startMonitoring() {
        samplingJob?.cancel()
        samplingJob = externalScope.launch(Dispatchers.IO) {
            while (isActive) {
                val realState = collectRealMetrics()
                _telemetryState.value = realState
                
                updateQueueSize(realState.runtime.eventBusQueueSize)
                
                // 1. PUBLICAR EVENTO NUEVO (Para la nueva arquitectura)
                CoreEventBus.publish(OmniEvent.HardwareTelemetryEmitted(realState))
                
                // 2. PUENTE LEGACY: Traducimos hardware real para la UI antigua
                val totalRam = if (realState.ram.totalBytes > 0) realState.ram.totalBytes.toFloat() else 1f
                val usedRam = realState.ram.runtimeUsedBytes.toFloat()
                
                val legacyEntry = TelemetryEntry(
                    cpuUsage = realState.cpu.usagePercentage,
                    ramUsage = (usedRam / totalRam) * 100f,
                    temperature = realState.battery.temperatureC,
                    timestamp = realState.timestamp
                )
                CoreEventBus.publish(OmniEvent.TelemetryEmitted(legacyEntry))
                
                delay(sampleIntervalMs)
            }
        }
    }

    fun stopMonitoring() {
        samplingJob?.cancel()
        samplingJob = null
    }

    fun updateSamplingInterval(intervalMs: Long) {
        this.sampleIntervalMs = intervalMs
    }

    fun incrementCoroutineCount() = coroutineCounter.incrementAndGet()
    fun decrementCoroutineCount() = coroutineCounter.decrementAndGet()
    fun updateQueueSize(size: Int) = eventBusQueueCounter.set(size)
    
    fun recordPluginLatency(latencyMs: Long) {
        totalLatencyAccumulator.addAndGet(latencyMs.toInt())
        pluginExecutionCount.incrementAndGet()
    }

    private fun createInitialState(): HardwareState {
        val profile = DeviceProfile(
            oem = Build.MANUFACTURER,
            model = Build.MODEL,
            apiLevel = Build.VERSION.SDK_INT,
            isBackgroundRestricted = false
        )
        return HardwareState(
            cpu = CpuMetrics(Runtime.getRuntime().availableProcessors(), 0f, 0),
            ram = RamMetrics(0L, 0L, 0L, MemoryPressure.LOW),
            battery = BatteryMetrics(100, false, 0, 0f, false),
            network = NetworkMetrics(NetworkQuality.DISCONNECTED, "NONE", 0),
            runtime = RuntimeInternalMetrics(0, 0, 0L, 0),
            profile = profile,
            thermal = ThermalState.COOL,
            timestamp = System.currentTimeMillis()
        )
    }

    private fun collectRealMetrics(): HardwareState {
        val memInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memInfo)

        val runtime = Runtime.getRuntime()
        val usedMem = runtime.totalMemory() - runtime.freeMemory()

        val ramPressure = when {
            memInfo.lowMemory -> MemoryPressure.CRITICAL
            memInfo.availMem < memInfo.totalMem * 0.15 -> MemoryPressure.HIGH
            memInfo.availMem < memInfo.totalMem * 0.35 -> MemoryPressure.MEDIUM
            else -> MemoryPressure.LOW
        }

        val ramMetrics = RamMetrics(
            totalBytes = memInfo.totalMem,
            availableBytes = memInfo.availMem,
            runtimeUsedBytes = usedMem,
            pressure = ramPressure
        )

        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val pct = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        val isCharging = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_STATUS) == BatteryManager.BATTERY_STATUS_CHARGING
        
        val batteryMetrics = BatteryMetrics(
            percentage = pct,
            isCharging = isCharging,
            voltageMv = 0,
            temperatureC = 0f, 
            isPowerSaverMode = powerManager.isPowerSaveMode
        )

        val activeNetwork = connectivityManager.activeNetwork
        val caps = connectivityManager.getNetworkCapabilities(activeNetwork)
        val (quality, transport) = if (caps != null) {
            val t = when {
                caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "CELLULAR"
                caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "WIFI"
                caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "ETHERNET"
                else -> "OTHER"
            }
            val q = when {
                caps.linkDownstreamBandwidthKbps > 15000 -> NetworkQuality.EXCELLENT
                caps.linkDownstreamBandwidthKbps > 5000 -> NetworkQuality.GOOD
                else -> NetworkQuality.POOR
            }
            Pair(q, t)
        } else {
            Pair(NetworkQuality.DISCONNECTED, "NONE")
        }

        val networkMetrics = NetworkMetrics(
            quality = quality,
            transportType = transport,
            linkDownstreamBandwidthKbps = caps?.linkDownstreamBandwidthKbps ?: 0
        )

        val thermal = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            when (powerManager.currentThermalStatus) {
                PowerManager.THERMAL_STATUS_NONE -> ThermalState.COOL
                PowerManager.THERMAL_STATUS_LIGHT -> ThermalState.MODERATE
                PowerManager.THERMAL_STATUS_MODERATE -> ThermalState.WARM
                PowerManager.THERMAL_STATUS_SEVERE -> ThermalState.THROTTLING
                PowerManager.THERMAL_STATUS_CRITICAL, 
                PowerManager.THERMAL_STATUS_EMERGENCY, 
                PowerManager.THERMAL_STATUS_SHUTDOWN -> ThermalState.CRITICAL
                else -> ThermalState.COOL
            }
        } else {
            ThermalState.COOL
        }

        val activeThreads = Thread.activeCount()
        val cpuUsage = estimateCpuUsage()

        val cpuMetrics = CpuMetrics(
            coreCount = runtime.availableProcessors(),
            usagePercentage = cpuUsage,
            activeThreads = activeThreads
        )

        val execCount = pluginExecutionCount.getAndSet(0)
        val avgLat = totalLatencyAccumulator.getAndSet(0)
        val runtimeMetrics = RuntimeInternalMetrics(
            activeCoroutines = coroutineCounter.get(),
            eventBusQueueSize = eventBusQueueCounter.get(),
            avgPluginLatencyMs = if (execCount > 0) (avgLat / execCount).toLong() else 0L,
            processingThroughput = execCount
        )

        val isBgRestricted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            activityManager.isBackgroundRestricted
        } else {
            false
        }

        return HardwareState(
            cpu = cpuMetrics,
            ram = ramMetrics,
            battery = batteryMetrics,
            network = networkMetrics,
            runtime = runtimeMetrics,
            profile = DeviceProfile(Build.MANUFACTURER, Build.MODEL, Build.VERSION.SDK_INT, isBgRestricted),
            thermal = thermal,
            timestamp = System.currentTimeMillis()
        )
    }

    private fun estimateCpuUsage(): Float {
        return try {
            val reader = RandomAccessFile("/proc/stat", "r")
            val load = reader.readLine()
            val toks = load.split(" +".toRegex())
            val idle = toks[4].toLong()
            val cpu = toks[1].toLong() + toks[2].toLong() + toks[3].toLong() + toks[5].toLong() + toks[6].toLong() + toks[7].toLong()
            reader.close()

            val totalDiff = (cpu + idle) - lastCpuTime
            val cpuDiff = cpu - lastAppCpuTime

            if (lastCpuTime == 0L) {
                lastCpuTime = cpu + idle
                lastAppCpuTime = cpu
                return 0.0f
            }

            lastCpuTime = cpu + idle
            lastAppCpuTime = cpu

            if (totalDiff > 0) ((cpuDiff.toFloat() / totalDiff.toFloat()) * 100f).coerceIn(0f, 100f) else 0.0f
        } catch (e: Exception) {
            val availableProcessors = Runtime.getRuntime().availableProcessors().toFloat()
            val loadedThreads = Thread.activeCount().toFloat()
            (loadedThreads / (availableProcessors * 12f) * 100f).coerceIn(5f, 90f)
        }
    }
}
