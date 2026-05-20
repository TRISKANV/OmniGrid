package com.tuapp.calculadora.ui.dashboard

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

// --- ARCHITECTURE DEFINITIONS ---
enum class ModuleSize { SMALL, WIDE }

data class DashboardPlugin(
    val id: String,
    val title: String,
    val size: ModuleSize,
    val content: @Composable (metrics: RuntimeMetrics) -> Unit
)

// --- MAIN RUNTIME DASHBOARD ---
@Composable
fun ModularDashboardScreen() {
    var diagnosticsOpen by remember { mutableStateOf(false) }
    
    // Recolección centralizada de las métricas de hardware y de los hilos del Core
    val sysMetrics by RuntimeTelemetryManager.metrics.collectAsState()

    // Bucle cíclico aislado de refresco de hardware de bajo coste (evita saturar el hilo UI)
    LaunchedEffect(Unit) {
        while (true) {
            RuntimeTelemetryManager.updateSystemMemory()
            delay(1000) // Muestreo de memoria cada 1 segundo exacto
        }
    }

    val activePlugins = remember {
        listOf(
            DashboardPlugin("sys_health", "RUNTIME HEALTH", ModuleSize.WIDE) { metrics ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("JVM STACK STORAGE", color = TacticalColors.TextSecondary, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                        Text(String.format("%.1f MB", metrics.memoryUsageMb), color = TacticalColors.TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    }
                    TinyWaveform()
                }
            },
            DashboardPlugin("net_flow", "ACTIVE SCHEDULERS", ModuleSize.SMALL) { metrics ->
                Column {
                    Text("${metrics.activeCoroutines} Coroutines", color = TacticalColors.TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    Text("Background Scope", color = TacticalColors.TextSecondary, fontSize = 11.sp)
                }
            },
            DashboardPlugin("queue", "DATA PIPELINE QUEUE", ModuleSize.SMALL) { metrics ->
                Column {
                    Text("${metrics.queueSize} / 128", color = TacticalColors.TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    Text("Buffer Status: NOMINAL", color = Color(0xFF00FF66), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TacticalColors.OledBlack)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            
            // --- RUNTIME TOP BAR ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, start = 16.dp, end = 16.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "OMNIGRID // ECOSYSTEM",
                        color = TacticalColors.TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "ENGINE STATE: ${sysMetrics.transportState}",
                        color = if (sysMetrics.transportState == "STANDBY") TacticalColors.TextSecondary else Color(0xFF00E5FF),
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
                        text = if(diagnosticsOpen) "HUD_ACTIVE" else "OBSERVE_ENV //",
                        color = if(diagnosticsOpen) TacticalColors.SystemWarning else TacticalColors.TextSecondary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // --- GRID ASIMÉTRICO DE PLUGINS REACCIONANDO AL BUS ---
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalItemSpacing = 12.dp,
                modifier = Modifier.fillMaxSize().weight(1f)
            ) {
                items(activePlugins, key = { it.id }) { plugin ->
                    PluginContainer(plugin = plugin, metrics = sysMetrics)
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
fun PluginContainer(plugin: DashboardPlugin, metrics: RuntimeMetrics) {
    val modifier = if (plugin.size == ModuleSize.WIDE) {
        Modifier.fillMaxWidth()
    } else {
        Modifier.aspectRatio(1f)
    }

    Box(
        modifier = modifier
            .tacticalGlass(cornerRadius = 12f)
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = plugin.title,
                    color = TacticalColors.TextSecondary,
                    fontSize = 10.sp,
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.SemiBold
                )
                if (plugin.id == "sys_health") {
                    BreathingIndicator()
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            plugin.content(metrics)
        }
    }
}
