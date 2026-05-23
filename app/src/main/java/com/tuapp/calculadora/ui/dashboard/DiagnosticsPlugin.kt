package com.tuapp.calculadora.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tuapp.calculadora.core.*
import com.tuapp.calculadora.core.plugin.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.UUID

/**
 * Estructura de datos interna aislada para el plugin.
 * Incluye un UUID inmutable para garantizar un rendering hiper-eficiente en LazyColumn.
 */
data class DiagnosticLog(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val tag: String,
    val message: String,
    val color: Color
)

/**
 * EL PACIENTE CERO.
 * Primer plugin oficial del entorno operativo OmniGrid.
 * Responsable de la observabilidad y renderizado en tiempo real del Kernel.
 */
class DiagnosticsPlugin : OmniPlugin {

    private val pluginScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var telemetryJob: Job? = null

    // 1. Manifiesto del Plugin
    override val manifest = PluginManifest(
        pluginId = "core.diagnostics",
        version = "1.0.0",
        capabilities = setOf(
            PluginCapability.DIAGNOSTICS, 
            PluginCapability.UI_DASHBOARD_WIDGET, 
            PluginCapability.EVENT_STREAMING
        ),
        priority = 100, // Prioridad Máxima (OS Level)
        requiresForeground = true
    )

    // 2. Estado del Ciclo de Vida
    private val _state = MutableStateFlow(PluginState.IDLE)
    override val state: StateFlow<PluginState> = _state.asStateFlow()

    // 3. Estado de Datos Aislado (No usa variables globales)
    private val _logs = MutableStateFlow<List<DiagnosticLog>>(emptyList())

    // --- LIFECYCLE HOOKS ---

    override suspend fun initialize() {
        _state.value = PluginState.INITIALIZING
        addLog("SYSTEM", "Diagnostics Module Initialized", Color.DarkGray)
    }

    override suspend fun start() {
        _state.value = PluginState.RUNNING
        addLog("SYSTEM", "Diagnostics Module Active & Listening", Color(0xFF00FF66))
        
        // Se suscribe al motor central sin acoplarse a otras clases
        telemetryJob = pluginScope.launch {
            CoreEventBus.events.collectLatest { event ->
                processSystemEvent(event)
            }
        }
    }

    override suspend fun stop() {
        telemetryJob?.cancel()
        _state.value = PluginState.DISABLED
        addLog("SYSTEM", "Module Disabled", Color.DarkGray)
    }

    override suspend fun recover() {
        // Degradación elegante
        stop()
        _logs.value = emptyList()
        addLog("RECOVERY", "Diagnostics degraded gracefully. Restarting stream...", Color.Yellow)
        start()
    }

    // --- LÓGICA DE NEGOCIO PRIVADA ---

    private fun processSystemEvent(event: OmniEvent) {
        val newLog = when (event) {
            is HardwareWarning -> DiagnosticLog(
                tag = "WARN",
                message = "[${event.severity}] ${event.message}",
                color = Color(0xFFFF3333)
            )
            is ThermalStateChanged -> DiagnosticLog(
                tag = "THERMAL",
                message = "TRANSITION: ${event.oldState} -> ${event.newState}",
                color = Color(0xFFFFB300)
            )
            is PluginSystemEvent -> DiagnosticLog(
                tag = event.type.ifEmpty { "PLUGIN" },
                message = event.payload,
                color = Color(0xFF00E5FF)
            )
            is HardwareTelemetryEmitted -> DiagnosticLog(
                tag = "METRICS",
                message = event.state,
                color = Color(0xFF00FF66).copy(alpha = 0.6f)
            )
            is SessionChanged -> DiagnosticLog(
                tag = "SESSION",
                message = "STATE -> ${event.newState}",
                color = Color(0xFFB388FF)
            )
            is SystemEvent -> DiagnosticLog(
                tag = "CORE",
                message = event.message,
                color = Color.LightGray
            )
            else -> null
        }

        newLog?.let { addLog(it) }
    }

    private fun addLog(tag: String, message: String, color: Color) {
        addLog(DiagnosticLog(tag = tag, message = message, color = color))
    }

    private fun addLog(log: DiagnosticLog) {
        val currentList = _logs.value.toMutableList()
        currentList.add(0, log)
        if (currentList.size > 50) { 
            currentList.removeLast() 
        }
        _logs.value = currentList
    }

    // --- UI DYNAMIC RENDERING ---

    @Composable
    override fun RenderWidget() {
        val logs by _logs.collectAsState()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 200.dp, max = 350.dp) // Widget de altura dinámica
                .background(Color(0xFF050505), RoundedCornerShape(8.dp))
                .padding(12.dp)
        ) {
            Text(
                text = "REAL_TIME_DIAGNOSTICS_STREAM :: [${manifest.pluginId}]",
                color = Color.White,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            // LazyColumn hiper-optimizada con recomposición mínima usando llaves UUID
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = logs,
                    key = { it.id } 
                ) { log ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF111111), RoundedCornerShape(4.dp))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "[${log.tag}]",
                            color = log.color,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(75.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = log.message,
                            color = Color.LightGray,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 14.sp
                        )
                    }
                }
            }
        }
    }
}
