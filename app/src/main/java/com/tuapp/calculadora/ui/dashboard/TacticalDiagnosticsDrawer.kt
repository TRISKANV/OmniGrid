package com.tuapp.calculadora.ui.dashboard

import android.os.Build
import android.graphics.RenderEffect
import android.graphics.Shader
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tuapp.calculadora.ui.system.*

@Composable
fun TacticalDiagnosticsDrawer(
    visible: Boolean,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Recolectar la historia de logs y métricas del bus global en tiempo real
    val logList by RuntimeTelemetryManager.logHistory.collectAsState()
    val sysMetrics by RuntimeTelemetryManager.metrics.collectAsState()

    AnimatedVisibility(
        visible = visible,
        enter = slideInHorizontally(
            initialOffsetX = { it }, 
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = 0.85f)
        ) + fadeIn(),
        exit = slideOutHorizontally(
            targetOffsetX = { it }, 
            animationSpec = spring(stiffness = Spring.StiffnessMedium, dampingRatio = 0.9f)
        ) + fadeOut(),
        modifier = modifier.fillMaxHeight().fillMaxWidth(0.85f)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        renderEffect = RenderEffect.createBlurEffect(
                            25f, 25f, Shader.TileMode.CLAMP
                        ).asComposeRenderEffect()
                    }
                }
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Color(0xCC050505), Color(0xF20A0B0C))
                    )
                )
                .border(1.dp, TacticalColors.BorderGlass, RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                
                // --- HEADER ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "TACTICAL RUNTIME HUD",
                            color = TacticalColors.TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )
                        Text(
                            text = "OBSERVER_LAYER // SYSTEM_CONNECTED",
                            color = TacticalColors.TextSecondary,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    
                    Box(
                        modifier = Modifier
                            .border(1.dp, TacticalColors.BorderGlass, RoundedCornerShape(4.dp))
                            .background(Color(0x1AFFFFFF))
                            .tacticalClick { onClose() }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("CLOSE //", color = TacticalColors.TextPrimary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // --- SECCIÓN 1: METRICAS REALES DESACOPLADAS ---
                Text("LIVE TELEMETRY", color = TacticalColors.TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IsolatedTelemetryWidget(modifier = Modifier.weight(1f), title = "ACTIVE_COROUTINES", unit = "JOBS") {
                        Text("${sysMetrics.activeCoroutines} active", color = TacticalColors.TextPrimary, fontSize = 16.sp, fontFamily = FontFamily.Monospace)
                    }
                    IsolatedTelemetryWidget(modifier = Modifier.weight(1f), title = "JVM_MEM_USAGE", unit = "MB") {
                        Text(String.format("%.2f MB", sysMetrics.memoryUsageMb), color = TacticalColors.TextPrimary, fontSize = 16.sp, fontFamily = FontFamily.Monospace)
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IsolatedTelemetryWidget(modifier = Modifier.weight(1f), title = "QUEUE_LOAD", unit = "BUF") {
                        Text("${sysMetrics.queueSize} / 128", color = TacticalColors.TextSecondary, fontSize = 15.sp, fontFamily = FontFamily.Monospace)
                    }
                    IsolatedTelemetryWidget(modifier = Modifier.weight(1f), title = "ENGINE_RUN_THROUGHPUT", unit = "OPS") {
                        Text("${sysMetrics.totalExecutions} total", color = Color(0xFF00E5FF), fontSize = 15.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // --- SECCIÓN 2: PIPELINE DE EVENTOS CENTRALIZADO ---
                Text("REALTIME EVENT STREAM", color = TacticalColors.TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(8.dp))
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(Color(0xFF030303))
                        .border(1.dp, TacticalColors.BorderGlass, RoundedCornerShape(6.dp))
                        .padding(10.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        reverseLayout = true
                    ) {
                        items(logList.reversed()) { log ->
                            val colorLevel = when (log.level) {
                                LogLevel.WARN -> TacticalColors.SystemWarning
                                LogLevel.CRITICAL -> Color.Red
                                LogLevel.EXEC -> Color(0xFF00E5FF)
                                LogLevel.INFO -> TacticalColors.TextSecondary
                            }
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text("[${log.timestamp}] ", color = TacticalColors.TextSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                Text("${log.tag}: ", color = colorLevel, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                Text(log.message, color = TacticalColors.TextPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun IsolatedTelemetryWidget(
    modifier: Modifier = Modifier,
    title: String,
    unit: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .background(Color(0x33000000))
            .border(1.dp, TacticalColors.BorderGlass, RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(title, color = TacticalColors.TextSecondary, fontSize = 8.sp, fontWeight = FontWeight.Bold)
            Text(unit, color = TacticalColors.TextSecondary, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
        }
        Spacer(modifier = Modifier.height(8.dp))
        content()
    }
}
