package com.tuapp.calculadora.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tuapp.calculadora.ui.system.*
import com.tuapp.calculadora.ui.system.hal.OmniDeviceHAL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TacticalDiagnosticsDrawer(modifier: Modifier = Modifier) {
    val logs by RuntimeTelemetryManager.logs.collectAsState()
    val session = SessionOrchestrator.getSessionManifest()
    val thermal = OmniDeviceHAL.fetchThermalProfile()
    val memory = OmniDeviceHAL.fetchMemoryProfile()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A)) // Fondo ultra oscuro
            .border(1.dp, Color(0xFF222222))
            .padding(16.dp)
    ) {
        // --- CABECERA DE DIAGNÓSTICO ---
        Text(
            text = "SYSTEM DIAGNOSTICS",
            color = Color(0xFF00FF66),
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 2.sp
        )
        Spacer(modifier = Modifier.height(16.dp))

        // --- MÉTRICAS EN TIEMPO REAL ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            MetricBlock(label = "SESSION UPTIME", value = "${session?.uptimeMs ?: 0}ms")
            MetricBlock(label = "ACTIVE TRANS", value = session?.activeTransport ?: "NONE")
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            MetricBlock(label = "CORE TEMP", value = "${thermal.cpuTempC}°C", alert = thermal.isThrottling)
            MetricBlock(label = "MEM PRESSURE", value = "${memory.pressurePercent}%", alert = memory.pressurePercent > 80)
        }

        Spacer(modifier = Modifier.height(20.dp))
        Divider(color = Color(0xFF222222), thickness = 1.dp)
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "EVENT STREAM BUFFER",
            color = Color.Gray,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.height(8.dp))

        // --- RENDERIZADO DE LOGS ---
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF111111), RoundedCornerShape(8.dp))
                .border(1.dp, Color(0xFF1A1A1A), RoundedCornerShape(8.dp))
                .padding(8.dp)
        ) {
            items(logs) { log ->
                val color = when (log.level) {
                    LogLevel.INFO -> Color(0xFF00FF66)
                    LogLevel.WARN -> Color(0xFFFFCC00)
                    LogLevel.CRITICAL -> Color(0xFFFF3333)
                    LogLevel.EXEC -> Color(0xFF00E5FF)
                }

                val timeFormatted = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date(log.timestamp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.Top // FIX: En Compose es verticalAlignment, no crossAxisAlignment
                ) {
                    Text(
                        text = "[$timeFormatted]",
                        color = Color.DarkGray,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.width(75.dp)
                    )
                    Text(
                        text = "[${log.tag}]",
                        color = color.copy(alpha = 0.8f),
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.width(45.dp)
                    )
                    Text(
                        text = log.message,
                        color = color,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

// Sub-componente táctico para mostrar las métricas en recuadros
@Composable
private fun MetricBlock(label: String, value: String, alert: Boolean = false) {
    val accentColor = if (alert) Color(0xFFFF3333) else Color(0xFF00E5FF)
    Column(
        modifier = Modifier
            .background(Color(0xFF111111), RoundedCornerShape(4.dp))
            .border(1.dp, if (alert) accentColor else Color.Transparent, RoundedCornerShape(4.dp))
            .padding(8.dp)
            .width(130.dp)
    ) {
        Text(
            text = label,
            color = Color.Gray,
            fontSize = 8.sp,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            color = accentColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}
