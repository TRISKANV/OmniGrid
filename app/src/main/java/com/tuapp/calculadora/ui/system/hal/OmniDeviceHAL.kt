package com.tuapp.calculadora.ui.system.hal

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager

data class MemoryProfile(val availableMB: Long, val totalMB: Long, val pressurePercent: Int, val isLowMemory: Boolean)
data class ThermalProfile(val cpuTempC: Float, val isThrottling: Boolean, val batteryLevel: Int)

object OmniDeviceHAL {
    
    fun getMemoryProfile(context: Context): MemoryProfile {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)

        val availableMB = memoryInfo.availMem / (1024 * 1024)
        val totalMB = memoryInfo.totalMem / (1024 * 1024)
        val pressure = if (totalMB > 0) ((totalMB - availableMB).toFloat() / totalMB.toFloat() * 100).toInt() else 0

        return MemoryProfile(
            availableMB = availableMB,
            totalMB = totalMB,
            pressurePercent = pressure,
            isLowMemory = memoryInfo.lowMemory
        )
    }

    fun getThermalProfile(context: Context): ThermalProfile {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val batteryPct = if (scale > 0) (level * 100) / scale else -1

        // La temperatura de batería es un proxy excelente y seguro de obtener para el thermal throttling de CPU
        val tempTenths = intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0
        val tempC = tempTenths / 10.0f

        // Heurística de degradación táctica:
        val isThrottling = tempC >= 39.0f || batteryPct in 1..15

        return ThermalProfile(
            cpuTempC = tempC,
            isThrottling = isThrottling,
            batteryLevel = batteryPct
        )
    }
}
