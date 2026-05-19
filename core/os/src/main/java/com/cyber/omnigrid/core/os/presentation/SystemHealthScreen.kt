package com.cyber.omnigrid.core.os.presentation

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cyber.omnigrid.core.designsystem.components.CyberCard
import com.cyber.omnigrid.core.designsystem.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SystemHealthScreen(
    viewModel: SystemHealthViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val logListState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(state.logs.size) {
        if (state.logs.isNotEmpty()) {
            scope.launch {
                logListState.animateScrollToItem(state.logs.lastIndex)
            }
        }
    }

    Scaffold(
        containerColor = TrueBlack
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- HEADER ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "// OMNIGRID_HEALTH_CENTER", color = CyberAccent, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    Text(text = "SYSTEM MONITOR", color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
                Button(
                    onClick = { viewModel.refreshAll() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    modifier = Modifier.border(1.dp, CyberAccent, RoundedCornerShape(4.dp)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("REFRESH", color = CyberAccent, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                // --- OEM & ENVIRONMENT RUNTIME ---
                item {
                    CyberCard(modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(text = "HARDWARE ARCHITECTURE", color = TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            RowValue("MANUFACTURER", state.deviceManufacturer.uppercase())
                            RowValue("MODEL_FINGERPRINT", state.deviceModel.uppercase())
                            RowValue("ANDROID_API", "${state.apiLevel}")
                        }
                    }
                }

                // --- PERSISTENCE FOREGROUND VALIDATION ---
                item {
                    CyberCard(modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(text = "FOREGROUND SERVICE READINESS", color = TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            
                            val backgroundStatus = if (state.isIgnoringBatteryOptimizations) "OPTIMIZACIONES EXCLUIDAS" else "RESTRICCION ACTIVA (OEM WHITELIST)"
                            val backgroundStatusColor = if (state.isIgnoringBatteryOptimizations) Color(0xFF00FF66) else Color(0xFFFFCC00)
                            
                            RowValue("BATTERY_POLICY", backgroundStatus, color = backgroundStatusColor)
                            
                            if (!state.isIgnoringBatteryOptimizations) {
                                Text(
                                    text = "Atención: Fabricantes como Xiaomi o Samsung pueden matar el motor síncrono si no se desactiva la optimización manualmente.",
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                                Button(
                                    onClick = {
                                        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                            data = Uri.parse("package:${context.packageName}")
                                        }
                                        context.startActivity(intent)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = DarkGrayCard),
                                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp).border(1.dp, BorderGray, RoundedCornerShape(4.dp))
                                ) {
                                    Text("DESACTIVAR OPTIMIZACIÓN (BYPASS OEM)", color = TextPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                }
                            }
                        }
                    }
                }

                // --- TELEMETRÍA ASÍNCRONA / LIVE METRICS ---
                item {
                    CyberCard(modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(text = "ENGINE LATENCY & METRICS", color = TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            RowValue("TX_LATENCY", "${state.metrics.txLatencyMs} ms")
                            RowValue("RX_LATENCY", "${state.metrics.rxLatencyMs} ms")
                            RowValue("MEMORY_CONSUMPTION", "${(state.metrics.totalMemoryBytes - state.metrics.freeMemoryBytes) / 1024 / 1024} MB")
                        }
                    }
                }

                // --- LIVE SYSTEM LOGS TERMINAL ---
                item {
                    Text(text = "CORE TELEMETRY LOGS", color = TextSecondary, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                }

                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .background(Color(0xFF020202), RoundedCornerShape(6.dp))
                            .border(1.dp, BorderGray, RoundedCornerShape(6.dp))
                            .padding(8.dp)
                    ) {
                        LazyColumn(
                            state = logListState,
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(state.logs, key = { it.id }) { log ->
                                LogTerminalLine(log)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RowValue(label: String, value: String, color: Color = CyberAccent) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "$label:", color = TextPrimary, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
        Text(text = value, color = color, fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun LogTerminalLine(log: SystemLogLine) {
    val timeFormatter = remember { SimpleDateFormat("HH:mm:ss.SSS", Locale.US) }
    val levelColor = when (log.level) {
        "ERROR" -> Color(0xFFFF3333)
        "WARN" -> Color(0xFFFFCC00)
        else -> Color(0xFF00FF66)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = timeFormatter.format(Date(log.timestamp)),
            color = DarkGrayCard,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = "[${log.tag}]",
            color = levelColor,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = log.message,
            color = TextPrimary,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.weight(1f)
        )
    }
}
