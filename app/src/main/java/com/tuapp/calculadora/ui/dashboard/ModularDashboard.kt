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

// ==========================================
// ARCHITECTURE DEFINITIONS
// ==========================================
enum class ModuleSize {
    SMALL, // Cuadrado, ocupa 1 columna (ej: Hardware monitor)
    WIDE   // Rectangular, ocupa 2 columnas (ej: Network OSINT)
}

data class DashboardPlugin(
    val id: String,
    val title: String,
    val size: ModuleSize,
    val content: @Composable () -> Unit
)

// ==========================================
// MAIN DASHBOARD RUNTIME
// ==========================================
@Composable
fun ModularDashboardScreen() {
    // Estado unificado para el control del Diagnostics Drawer
    var diagnosticsOpen by remember { mutableStateOf(false) }

    val activePlugins = listOf(
        DashboardPlugin("sys_health", "RUNTIME HEALTH", ModuleSize.WIDE) { HealthModuleContent() },
        DashboardPlugin("net_flow", "ACTIVE FLOWS", ModuleSize.SMALL) { Text("0 TCP", color = TacticalColors.TextSecondary, fontSize = 12.sp, fontFamily = FontFamily.Monospace) },
        DashboardPlugin("queue", "QUEUE SIZE", ModuleSize.SMALL) { Text("0/128", color = TacticalColors.TextSecondary, fontSize = 12.sp, fontFamily = FontFamily.Monospace) }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TacticalColors.OledBlack)
    ) {
        // Capa 1: Contenido del Dashboard Base
        Column(modifier = Modifier.fillMaxSize()) {
            
            // HUD Top-Bar Operativa
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, start = 16.dp, end = 16.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "OMNIGRID // ENGINE",
                        color = TacticalColors.TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "NODE STATUS: NOMINAL",
                        color = Color(0xFF00FF66),
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Disparador de Observabilidad
                Box(
                    modifier = Modifier
                        .border(1.dp, if(diagnosticsOpen) TacticalColors.SystemWarning else TacticalColors.BorderGlass, RoundedCornerShape(4.dp))
                        .background(if(diagnosticsOpen) TacticalColors.SystemWarning.copy(alpha = 0.1f) else Color.Transparent)
                        .tacticalClick { diagnosticsOpen = !diagnosticsOpen }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if(diagnosticsOpen) "DIAG_ACTIVE" else "DIAGNOSTICS //",
                        color = if(diagnosticsOpen) TacticalColors.SystemWarning else TacticalColors.TextSecondary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Grid Asimétrico de Plugins Visuales
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalItemSpacing = 12.dp,
                modifier = Modifier.fillMaxSize().weight(1f)
            ) {
                items(activePlugins, key = { it.id }) { plugin ->
                    PluginContainer(plugin = plugin)
                }
            }
        }

        // Capa 2: Efectos Cinemáticos Ambientales Subconscientes
        ScanlineOverlay(modifier = Modifier.fillMaxSize())

        // Capa 3: Diagnostics Drawer (Efecto Glass superior con Blur Localizado)
        TacticalDiagnosticsDrawer(
            visible = diagnosticsOpen,
            onClose = { diagnosticsOpen = false },
            modifier = Modifier.align(Alignment.CenterEnd)
        )
    }
}

// ==========================================
// PLUGIN RENDERER
// ==========================================
@Composable
fun PluginContainer(plugin: DashboardPlugin) {
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
            plugin.content()
        }
    }
}

// ==========================================
// WIDGET CONTENT DEFINITIONS
// ==========================================
@Composable
private fun HealthModuleContent() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("SYS_UPTIME", color = TacticalColors.TextSecondary, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
            Text("04:12:09", color = TacticalColors.TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.SansSerif)
        }
        TinyWaveform()
    }
}
