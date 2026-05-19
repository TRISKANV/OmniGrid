package com.cyber.omnigrid.feature.automation.presentation.execution

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cyber.omnigrid.core.designsystem.components.CyberCard
import com.cyber.omnigrid.core.designsystem.theme.*
import com.cyber.omnigrid.feature.automation.domain.engine.ExecutionStatus
import com.cyber.omnigrid.feature.automation.domain.engine.LogLevel
import kotlinx.coroutines.launch

@Composable
fun LiveExecutionScreen(
    viewModel: ExecutionViewModel,
    scriptContent: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Lanzar la inyección inmediatamente al renderizar la pantalla
    LaunchedEffect(Unit) {
        viewModel.startExecution(scriptContent)
    }

    // Auto-scroll reactivo optimizado al ingresar nuevos logs
    LaunchedEffect(state.logs.size) {
        if (state.logs.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(state.logs.size - 1)
            }
        }
    }

    // Definición de colores dinámicos según el estado del hardware
    val statusColor = when (state.status) {
        ExecutionStatus.CONNECTING, ExecutionStatus.RUNNING -> CyberAccent
        ExecutionStatus.SUCCESS -> Color(0xFF00FF66)
        ExecutionStatus.ERROR -> Color(0xFFFF3333)
        ExecutionStatus.CANCELLED -> Color(0xFFFF9900)
        else -> TextSecondary
    }

    val animatedProgress by animateFloatAsState(targetValue = state.progressProgress)

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
            // --- TOP CONTROL PANEL ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "// ENGINE_EXECUTION_LIVE", color = CyberAccent, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    Text(text = "STATUS: ${state.status.name}", color = statusColor, fontSize = 16.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }

                if (state.status == ExecutionStatus.RUNNING || state.status == ExecutionStatus.CONNECTING) {
                    Button(
                        onClick = { viewModel.abortExecution() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF3333)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("ABORT", color = TrueBlack, fontSize = 12.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = onNavigateBack,
                        colors = ButtonDefaults.buttonColors(containerColor = DarkGrayCard),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.border(1.dp, BorderGray, RoundedCornerShape(8.dp))
                    ) {
                        Text("RETURN", color = TextPrimary, fontSize = 12.sp)
                    }
                }
            }

            // --- TELEMETRÍA Y HUD ---
            CyberCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "QUEUE PROGRESS", color = TextSecondary, fontSize = 10.sp)
                        Text(text = "${state.currentStep}/${state.totalSteps} OPERATIONS", color = CyberAccent, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    }
                    
                    LinearProgressIndicator(
                        progress = animatedProgress,
                        modifier = Modifier.fillMaxWidth().height(4.dp),
                        color = statusColor,
                        trackColor = DarkGrayCard
                    )
                }
            }

            // --- TERMINAL DE LOGS REACTIVA ---
            Text(text = "OUTPUT TERMINAL stream", color = TextSecondary, fontSize = 10.sp)
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color(0xFF030303), RoundedCornerShape(12.dp))
                    .border(1.dp, BorderGray, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(state.logs) { log ->
                        val logColor = when (log.level) {
                            LogLevel.INFO -> TextSecondary
                            LogLevel.WARN -> Color(0xFFFF9900)
                            LogLevel.ERROR -> Color(0xFFFF3333)
                            LogLevel.SUCCESS -> Color(0xFF00FF66)
                            LogLevel.PAYLOAD_STEP -> CyberAccent
                        }
                        
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "[+] ",
                                color = BorderGray,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = log.message,
                                color = logColor,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
