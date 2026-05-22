package com.tuapp.calculadora.ui.dashboard

import com.tuapp.calculadora.core.*
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
import kotlinx.coroutines.flow.collectLatest

data class DiagnosticLog(
    val timestamp: Long,
    val tag: String,
    val message: String,
    val color: Color
)

@Composable
fun TacticalDiagnosticsDrawer(modifier: Modifier = Modifier) {
    val logs = remember { mutableStateListOf<DiagnosticLog>() }

    LaunchedEffect(Unit) {
        CoreEventBus.events.collectLatest { event ->
            val newLog = when (event) {
                is HardwareWarning -> DiagnosticLog(
                    timestamp = System.currentTimeMillis(),
                    tag = "WARN",
                    message = event.message,
                    color = Color(0xFFFF3333)
                )
                is ThermalStateChanged -> DiagnosticLog(
                    timestamp = System.currentTimeMillis(),
                    tag = "THERMAL",
                    message = "TRANSITION: ${event.oldState} -> ${event.newState}",
                    color = Color(0xFFFFB300)
                )
                is PluginSystemEvent -> DiagnosticLog(
                    timestamp = System.currentTimeMillis(),
                    tag = event.type.ifEmpty { "PLUGIN" },
                    message = event.payload.ifEmpty { "No descriptor payload available" },
                    color = Color(0xFF00E5FF)
                )
                is HardwareTelemetryEmitted -> DiagnosticLog(
                    timestamp = System.currentTimeMillis(),
                    tag = "METRICS",
                    message = "HARDWARE STATE: ${event.state}",
                    color = Color(0xFF00FF66).copy(alpha = 0.5f)
                )
                else -> null
            }

            if (newLog != null) {
                logs.add(0, newLog)
                if (logs.size > 50) { logs.removeLast() } // Evitar leaks de memoria RAM
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF050505))
            .padding(16.dp)
    ) {
        Text(
            text = "REAL_TIME_DIAGNOSTICS_STREAM",
            color = Color.White,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(logs) { log ->
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
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}
