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
import com.tuapp.calculadora.ui.system.SessionOrchestrator
import com.tuapp.calculadora.ui.system.hal.RuntimeIntelligenceEngine
import com.tuapp.calculadora.ui.system.sdk.*
import kotlinx.coroutines.delay

// ==========================================================================
// MOCK PLUGIN PARA DEMOSTRAR LA NUEVA ARQUITECTURA SDK
// ==========================================================================
class CoreTelemetryPlugin : OmniPlugin {
    override val manifest = PluginManifest(
        pluginId = "core.telemetry.01",
        displayName = "CORE TELEMETRY",
        version = "2.0.0",
        description = "Provides real-time HAL observability",
        category = PluginCategory.TELEMETRY,
        providedCapabilities = setOf(SystemCapability.HARDWARE_TELEMETRY),
        consumedCapabilities = emptySet(),
        requiredPermissions = emptyList(),
        visualPriority = 0,
        supportsHeadlessExecution = true,
        transportCompatibility = listOf("LOCAL")
    )

    override val widgetProvider = object : PluginWidgetProvider {
        @Composable
        override fun Render(modifier: Modifier) {
            Column(modifier = modifier.padding(8.dp)) {
                Text("HAL DATA STREAM ONLINE", color = Color(0xFF00FF66), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                Spacer(modifier = Modifier.height(4.dp))
                Text("V 2.0.0 // NOMINAL", color = Color.Gray, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
            }
        }
        override fun onWidgetVisible() {}
        override fun onWidgetHidden() {}
    }

    override fun onInstall() {}
    override fun onBoot() {}
    override fun onSuspend() {}
    override fun onDestroy() {}
    override fun executeAction(actionId: String, payload: Map<String, Any>) = Result.success(Unit)
    override fun getHealthStatus() = "NOMINAL"
}

// ==========================================================================
// COMPOSITOR DEL ECOSISTEMA (DASHBOARD)
// ==========================================================================
@Composable
fun ModularDashboardScreen() {
    val anomalies by RuntimeIntelligenceEngine.anomalies.collectAsState()
    
    // Simulación del gestor de plugins cargando el sistema
    val loadedPlugins = remember {
        listOf(CoreTelemetryPlugin()).sortedBy { it.manifest.visualPriority }
    }

    LaunchedEffect(Unit) {
        SessionOrchestrator.bootstrapSession()
        loadedPlugins.forEach { it.onBoot() }
        
        while (true) {
            SessionOrchestrator.tick()
            delay(1000) // 1Hz Tick Rate del OS
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF050505)) // Tactical OLED Black
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            
            // --- TOP HUD: INTELLIGENCE & SESSION LAYER ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, start = 16.dp, end = 16.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "OMNI_OS // V2.0",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-0.5).sp
                    )
                    val session = SessionOrchestrator.getSessionManifest()
                    Text(
                        text = "SESSION: ${session?.sessionId ?: "BOOTING"} | ${session?.status?.name ?: "N/A"}",
                        color = Color(0xFF00E5FF),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }

                // AI Indicator
                Box(
                    modifier = Modifier
                        .border(1.dp, if(anomalies.isEmpty()) Color(0xFF00FF66) else Color(0xFFFF3333), RoundedCornerShape(4.dp))
                        .background(if(anomalies.isEmpty()) Color(0xFF00FF66).copy(alpha = 0.1f) else Color(0xFFFF3333).copy(alpha = 0.1f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if(anomalies.isEmpty()) "AI: NOMINAL" else "AI: ANOMALY",
                        color = if(anomalies.isEmpty()) Color(0xFF00FF66) else Color(0xFFFF3333),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // --- GRID DE PLUGINS DINÁMICO ---
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalItemSpacing = 12.dp,
                modifier = Modifier.fillMaxSize()
            ) {
                items(loadedPlugins, key = { it.manifest.pluginId }) { plugin ->
                    PluginContainer(plugin = plugin)
                }
            }
        }
    }
}

@Composable
fun PluginContainer(plugin: OmniPlugin) {
    val isWide = plugin.manifest.visualPriority == 0
    val modifier = if (isWide) Modifier.fillMaxWidth() else Modifier.aspectRatio(1f)

    Box(
        modifier = modifier
            .background(Color(0xFF111111), RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFF222222), RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = plugin.manifest.displayName,
                    color = Color.Gray,
                    fontSize = 10.sp,
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            
            // Renderizado agnóstico del widget del plugin
            plugin.widgetProvider?.Render(modifier = Modifier.fillMaxSize())
        }
    }
}
