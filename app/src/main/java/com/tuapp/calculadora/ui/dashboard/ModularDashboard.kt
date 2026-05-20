package com.tuapp.calculadora.ui.dashboard

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tuapp.calculadora.ui.system.*
import kotlinx.coroutines.delay

enum class ModuleSize { SMALL, WIDE }

// ==========================================================================
// MOTOR DE GESTIÓN Y ORQUESTACIÓN DE LA PLATAFORMA (DASHBOARD)
// ==========================================================================
@Composable
fun ModularDashboardScreen() {
    var diagnosticsOpen by remember { mutableStateOf(false) }
    val sysMetrics by RuntimeTelemetryManager.metrics.collectAsState()

    // Bucle unificado de muestreo y actualización del Core Platform
    LaunchedEffect(Unit) {
        RuntimeSessionManager.startSession("LOCAL_MESH")
        while (true) {
            RuntimeTelemetryManager.updateSystemStateDirect()
            delay(1000) // Latido rítmico de telemetría estable (1 Hz)
        }
    }

    // Inyección automatizada de Plugins del ecosistema ordenados por prioridad visual
    val registeredPlugins = remember {
        listOf(
            HardwareObserverPlugin(),
            SessionAnalyticsPlugin(),
            CoreEventBusPlugin()
        ).sortedBy { it.metadata.priority }
    }

    // Lógica estructural de ciclo de vida de conexión para los plugins activos
    DisposableEffect(registeredPlugins) {
        registeredPlugins.forEach { it.onPluginAttach() }
        onDispose {
            registeredPlugins.forEach { it.onPluginDetach() }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TacticalColors.OledBlack)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            
            // --- TOP HUD PLATFORM LAYER ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, start = 16.dp, end = 16.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "OMNI_PLATFORM // R1",
                        color = TacticalColors.TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "SESSION: ${sysMetrics.activeSessionId} [${sysMetrics.sessionDurationFormatted}]",
                        color = Color(0xFF00E5FF),
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .border(1.dp, if(diagnosticsOpen) TacticalColors.SystemWarning else TacticalColors.BorderGlass, RoundedCornerShape(4.dp))
                        .background(if(diagnosticsOpen) TacticalColors.SystemWarning.copy(alpha = 0.1f) else Color.Transparent)
                        .tacticalClick { diagnosticsOpen = !diagnosticsOpen }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if(diagnosticsOpen) "HUD_ACTIVE" else "DIAGNOSTICS //",
                        color = if(diagnosticsOpen) TacticalColors.SystemWarning else TacticalColors.TextSecondary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // --- GRID DE COMPOSICIÓN ADAPTATIVA DE PLUGINS ---
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalItemSpacing = 12.dp,
                modifier = Modifier.fillMaxSize().weight(1f)
            ) {
                items(registeredPlugins, key = { it.metadata.id }) { plugin ->
                    // Forzar tamaño WIDE para el primer plugin de prioridad alta o según su diseño interno
                    val determinedSize = if (plugin.metadata.priority == 0) ModuleSize.WIDE else ModuleSize.SMALL
                    
                    PluginFrame(plugin = plugin, size = determinedSize, metrics = sysMetrics)
                }
            }
        }

        ScanlineOverlay(modifier = Modifier.fillMaxSize())

        TacticalDiagnosticsDrawer(
            visible = diagnosticsOpen,
            onClose = { diagnosticsOpen = false },
            modifier = Modifier.align(Alignment.CenterEnd)
        )
    }
}

@Composable
fun PluginFrame(plugin: RuntimePlugin, size: ModuleSize, metrics: RuntimeMetrics) {
    val modifier = if (size == ModuleSize.WIDE) Modifier.fillMaxWidth() else Modifier.aspectRatio(1f)

    Box(
        modifier = modifier
            .tacticalGlass(cornerRadius = 12f)
            .padding(14.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = plugin.metadata.name,
                    color = TacticalColors.TextSecondary,
                    fontSize = 10.sp,
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.SemiBold
                )
                BreathingIndicator()
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            plugin.RenderWidget(size = size, systemMetrics = metrics)
        }
    }
}

// ==========================================================================
// IMPLEMENTACIONES DE PLUGINS REALES (CONTRATOS EJECUTADOS)
// ==========================================================================

class HardwareObserverPlugin : RuntimePlugin {
    override val metadata = PluginMetadata("hwd_monitor", "DEVICE_HARDWARE_LAYER", "1.0.0", 0)

    override fun onPluginAttach() {
        RuntimeEventBus.emit(RuntimeEvent.System("Hardware Observer Plugin mounted into active dashboard slots.", LogLevel.INFO))
    }

    override fun onPluginDetach() {}

    @Composable
    override fun RenderWidget(size: ModuleSize, systemMetrics: RuntimeMetrics) {
        val hw = systemMetrics.hardwareState
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("BATTERY_CORE", color = TacticalColors.TextSecondary, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                Text("${hw.batteryLevel}%", color = TacticalColors.TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("THERMAL: ${hw.thermalState}", color = if (hw.thermalState == "NOMINAL") Color(0xFF00FF66) else TacticalColors.SystemWarning, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("NETWORK_LINK", color = TacticalColors.TextSecondary, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                Text(hw.networkLink, color = Color(0xFF00E5FF), fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                Text("USB_OTG: ${if(hw.otgDetected) "READY" else "DISCONNECTED"}", color = TacticalColors.TextSecondary, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
            }
        }
    }
}

class SessionAnalyticsPlugin : RuntimePlugin {
    override val metadata = PluginMetadata("session_analytics", "PLATFORM_THROUGHPUT", "1.0.0", 1)

    override fun onPluginAttach() {}
    override fun onPluginDetach() {}

    @Composable
    override fun RenderWidget(size: ModuleSize, systemMetrics: RuntimeMetrics) {
        Column {
            Text("${systemMetrics.totalExecutions} OPS", color = TacticalColors.TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Engine Pipeline Ops", color = TacticalColors.TextSecondary, fontSize = 10.sp)
        }
    }
}

class CoreEventBusPlugin : RuntimePlugin {
    override val metadata = PluginMetadata("event_bus_monitor", "BUS_TELEMETRY", "1.0.0", 2)

    override fun onPluginAttach() {}
    override fun onPluginDetach() {}

    @Composable
    override fun RenderWidget(size: ModuleSize, systemMetrics: RuntimeMetrics) {
        Column {
            Text("QUEUE STATUS", color = TacticalColors.TextSecondary, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
            Text("${systemMetrics.queueSize} BUF", color = TacticalColors.TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            Spacer(modifier = Modifier.height(4.dp))
            TinyWaveform()
        }
    }
}
