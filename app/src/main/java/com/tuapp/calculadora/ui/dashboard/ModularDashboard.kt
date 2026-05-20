package com.tuapp.calculadora.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    // Estos módulos luego pueden venir inyectados o de un ViewModel
    val activePlugins = listOf(
        DashboardPlugin("sys_health", "RUNTIME HEALTH", ModuleSize.WIDE) { HealthModuleContent() },
        DashboardPlugin("net_flow", "ACTIVE FLOWS", ModuleSize.SMALL) { Text("0 TCP", color = TacticalColors.TextSecondary, fontSize = 12.sp) },
        DashboardPlugin("queue", "QUEUE SIZE", ModuleSize.SMALL) { Text("0/128", color = TacticalColors.TextSecondary, fontSize = 12.sp) }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TacticalColors.OledBlack)
    ) {
        // Capa 1: El contenido modular
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalItemSpacing = 12.dp,
            modifier = Modifier.fillMaxSize()
        ) {
            items(activePlugins, key = { it.id }) { plugin ->
                PluginContainer(plugin = plugin)
            }
        }

        // Capa 2: Efectos Ambientales (Subconscientes)
        ScanlineOverlay(modifier = Modifier.fillMaxSize())
    }
}

// ==========================================
// PLUGIN RENDERER
// ==========================================
@Composable
fun PluginContainer(plugin: DashboardPlugin) {
    // Definimos el comportamiento en el Grid según el tamaño
    val modifier = if (plugin.size == ModuleSize.WIDE) {
        Modifier.fillMaxWidth() // En un grid, esto se maneja con el Span en realidad, pero preparamos el Modifier
    } else {
        Modifier.aspectRatio(1f)
    }

    Box(
        modifier = modifier
            .tacticalGlass(cornerRadius = 12f)
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Plugin Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = plugin.title,
                    color = TacticalColors.TextSecondary,
                    fontSize = 10.sp,
                    letterSpacing = 1.sp
                )
                
                // Si es un módulo activo, le agregamos el pulso
                if (plugin.id == "sys_health") {
                    BreathingIndicator()
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Plugin Content Inyectado
            plugin.content()
        }
    }
}

// ==========================================
// MOCK MODULES (Para previsualizar la estructura)
// ==========================================
@Composable
private fun HealthModuleContent() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("SYS_UPTIME", color = TacticalColors.TextSecondary, fontSize = 10.sp)
            Text("04:12:09", color = TacticalColors.TextPrimary, fontSize = 24.sp)
        }
        
        // Nuestro nuevo componente HUD aislado
        TinyWaveform()
    }
}
