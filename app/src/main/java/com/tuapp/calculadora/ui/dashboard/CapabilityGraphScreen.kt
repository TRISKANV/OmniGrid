package com.tuapp.calculadora.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tuapp.calculadora.ui.system.sdk.OmniPlugin
import com.tuapp.calculadora.ui.system.sdk.SystemCapability

@Composable
fun CapabilityGraphScreen(activePlugins: List<OmniPlugin>) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        Text(
            text = "RUNTIME TOPOLOGY",
            color = Color(0xFF00E5FF),
            fontSize = 14.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Ejemplo visual de Grafo OLED-first
        activePlugins.forEach { plugin ->
            PluginNode(plugin)
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun PluginNode(plugin: OmniPlugin) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF222222), RoundedCornerShape(8.dp))
            .background(Color(0xFF0A0A0A), RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = plugin.manifest.displayName,
                color = Color.White,
                fontSize = 14.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = plugin.manifest.pluginId,
                color = Color.DarkGray,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Capabilities Edge
        Row {
            Text(text = "EXPORTS ──────► ", color = Color.DarkGray, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            Column {
                if (plugin.manifest.providedCapabilities.isEmpty()) {
                    Text("NONE", color = Color.Gray, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                } else {
                    plugin.manifest.providedCapabilities.forEach { cap ->
                        Text(cap.name, color = Color(0xFF00FF66), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }
    }
}
