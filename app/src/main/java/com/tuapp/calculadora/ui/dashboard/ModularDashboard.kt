package com.tuapp.calculadora.ui.dashboard

import com.tuapp.calculadora.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * MODULAR CYBERDECK DASHBOARD (HOME REAL).
 * Pantalla principal de OmniGrid. Consume telemetría pura, se conecta al bus de eventos central
 * y ofrece un panel de mandos premium táctico y minimalista en formato OLED-First.
 */
@Composable
fun ModularDashboard(
    onExitRuntime: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeSessionId = SessionOrchestrator.sessionId
    val currentOperator = SessionOrchestrator.name
    
    // Captura reactiva de eventos del Bus de manera aislada para optimizar recomposiciones
    val systemLogs = remember { mutableStateListOf<String>() }
    
    // Formateador de tiempo táctico de consola viva
    val timeFormatter = remember { SimpleDateFormat("HH:mm:ss.SSS", Locale.US) }

    LaunchedEffect(Unit) {
        systemLogs.add("[${timeFormatter.format(Date())}] SYS // CORE SYSTEM ONLINE")
        systemLogs.add("[${timeFormatter.format(Date())}] AUTH // SESSION VERIFIED: $activeSessionId")
        
        // Escucha directa del tejido de eventos reales del sistema operativo
        CoreEventBus.events.collectLatest { event ->
            val logMessage = when (event) {
                is SystemEvent -> "SYS // ${event.message.ifEmpty { "HARDWARE COCKPIT BOOTSTRAPPED" }}"
                is SessionChanged -> "AUTH // CORE SESSION STATE UPDATED TO: ${event.newState}"
                is VaultEvent -> "VAULT // CRYPTO VAULT STATE INVERSION: Locked=${event.isLocked}"
                is TelemetryEmitted -> "PAYLOAD // DEPLOYED TO ${event.targetServer} [SUCCESS=${event.success}] STATE: ${event.state}"
                is PayloadEvent -> "PAYLOAD // ENQUEUED DEPLOYMENT: ${event.type} -> ${event.payload}"
                is HardwareWarning -> "ALERT // ANOMALY INSIDE ${event.subsystem.uppercase()} SEVERITY: ${event.severity}"
                is HardwareTelemetryEmitted -> "HARDWARE // SENSOR STATE: ${event.state}"
                is ThermalStateChanged -> "THERMAL // CORE TEMPERATURE CHANGED: ${event.oldState} -> ${event.newState}"
                else -> "SYS // UNMAPPED RUNTIME EVENT RECEIVED"
            }
            systemLogs.add(0, "[${timeFormatter.format(Date())}] $logMessage")
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF000000)) // True Black OLED Background
            .padding(top = 40.dp, bottom = 24.dp, start = 20.dp, end = 20.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            
            // --- HEADER DE CONSOLA TÁCTICA ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "OMNIGRID // RUNTIME OS",
                        color = Color(0xFF00F0FF), // Cyber Cyan Neon Accent
                        fontSize = 18.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "OPERATOR: $currentOperator // SRC: REAL_HARDWARE",
                        color = Color(0xFF64748B),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                }
                
                // Indicador de Estado de Sesión en Vivo
                Box(
                    modifier = Modifier
                        .background(Color(0xFF091A1E), RoundedCornerShape(4.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "SYS_ACTIVE",
                        color = Color(0xFF22C55E), // Matrix Green
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- PANEL DE METADATOS SEGUROS ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF050B14))
                    .padding(12.dp)
            ) {
                Text(
                    text = "ID DE SESIÓN ÚNICA: $activeSessionId",
                    color = Color(0xFFE2E8F0),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "PERSISTENCIA DEL RUNTIME: COHERENTE // SECUREVAULT: READY",
                    color = Color(0xFF818CF8),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- LOG FEED EN TIEMPO REAL (CONSOLA VIVA) ---
            Text(
                text = "OPERATIONAL SYSTEM FEED:",
                color = Color(0xFF475569),
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color(0xFF020617))
                    .padding(8.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(systemLogs) { log ->
                        Text(
                            text = log,
                            color = if (log.contains("ALERT")) Color(0xFFEF4444) else Color(0xFF94A3B8),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- CONTROL INTERACTIVO DE PLATAFORMA ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        // Simular inyección de evento real de telemetría a través del bus para testing operativo
                        CoreEventBus.tryEmitEvent(
                            SystemEvent(message = "RUNTIME SYSTEM PING")
                        )
                        SessionOrchestrator.tick()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
                    border = BorderStroke(1.dp, Color(0xFF1E293B))
                ) {
                    Text(
                        text = "EMIT_PING",
                        color = Color(0xFF00F0FF),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp
                    )
                }

                Button(
                    onClick = {
                        // Disparar simulación de Payload asíncrono para verificar reactividad del bus centralizado
                        CoreEventBus.tryEmitEvent(
                            TelemetryEmitted(
                                targetServer = "srv-hub-alpha",
                                success = true,
                                state = "PL-99X"
                            )
                        )
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
                    border = BorderStroke(1.dp, Color(0xFF1E293B))
                ) {
                    Text(
                        text = "TEST_PAYLOAD",
                        color = Color(0xFF818CF8),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp
                    )
                }

                Button(
                    onClick = {
                        SessionOrchestrator.clearSession()
                        onExitRuntime()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1517)),
                    border = BorderStroke(1.dp, Color(0xFF991B1B))
                ) {
                    Text(
                        text = "KILL_OS",
                        color = Color(0xFFEF4444),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
