package com.tuapp.calculadora.ui.system

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tuapp.calculadora.ui.system.sdk.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// NOTA: Se eliminó la re-declaración de "enum class LogLevel" porque ya existe
// y se exporta desde RuntimeCorePlatform.kt

data class LogEntry(
    val timestamp: Long,
    val tag: String,
    val message: String,
    val level: LogLevel
)

// ==========================================================================
// 1. EL MOTOR DE TELEMETRÍA (Adaptado a OmniEventBus)
// ==========================================================================
object RuntimeTelemetryManager {
    private val _logs = MutableStateFlow<List<LogEntry>>(emptyList())
    val logs: StateFlow<List<LogEntry>> = _logs.asStateFlow()

    private var executionCount = 0

    fun log(tag: String, message: String, level: LogLevel = LogLevel.INFO) {
        val entry = LogEntry(java.lang.System.currentTimeMillis(), tag, message, level)
        _logs.update { currentList ->
            val newList = currentList.toMutableList()
            newList.add(0, entry)
            // Limitamos a 50 eventos en memoria para evitar el Memory Pressure
            if (newList.size > 50) newList.take(50) else newList
        }
    }

    fun registerExecution() {
        executionCount++
    }
}

// ==========================================================================
// 2. EL PLUGIN OFICIAL DE TELEMETRÍA DEL OMNI OS
// Transforma el viejo sistema estático en un plugin inyectable.
// ==========================================================================
class TelemetryCorePlugin : OmniPlugin {
    override val manifest = PluginManifest(
        pluginId = "sys.telemetry.01",
        displayName = "SYSTEM TELEMETRY",
        version = "2.1.0",
        description = "Provides real-time event logging and execution metrics.",
        category = PluginCategory.DIAGNOSTICS,
        providedCapabilities = setOf(SystemCapability.NETWORK_OBSERVABILITY),
        consumedCapabilities = emptySet(),
        requiredPermissions = emptyList(),
        visualPriority = 10, // Menos prioridad que el HAL, va abajo
        supportsHeadlessExecution = true,
        transportCompatibility = listOf("LOCAL", "EXTERNAL_BUS")
    )

    override val widgetProvider = object : PluginWidgetProvider {
        @Composable
        override fun Render(modifier: Modifier) {
            val logs by RuntimeTelemetryManager.logs.collectAsState()

            Column(modifier = modifier.fillMaxSize()) {
                if (logs.isEmpty()) {
                    Text(
                        text = "BUFFER EMPTY",
                        color = Color.DarkGray,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                } else {
                    logs.take(3).forEach { log ->
                        val color = when (log.level) {
                            LogLevel.CRITICAL -> Color(0xFFFF3333)
                            LogLevel.WARN -> Color(0xFFFFCC00)
                            LogLevel.EXEC -> Color(0xFF00E5FF)
                            else -> Color.Gray
                        }
                        Text(
                            text = "[${log.tag}] ${log.message}",
                            color = color,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                    }
                }
            }
        }

        override fun onWidgetVisible() {}
        override fun onWidgetHidden() {}
    }

    override fun onInstall() {
        RuntimeTelemetryManager.log("SYS", "Telemetry Plugin Installed", LogLevel.INFO)
    }

    override fun onBoot() {
        RuntimeTelemetryManager.log("SYS", "Telemetry Boot Sequence OK", LogLevel.INFO)
    }

    override fun onSuspend() {}
    override fun onDestroy() {}
    
    override fun executeAction(actionId: String, payload: Map<String, Any>): Result<Any> {
        return Result.failure(Exception("Action not supported"))
    }
    
    override fun getHealthStatus() = "ACTIVE"
}
