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
import kotlinx.coroutines.delay
import kotlin.random.Random

// Structure for Modular Telemetry Logs
data class RuntimeLog(val timestamp: String, val level: String, val message: String)

@Composable
fun TacticalDiagnosticsDrawer(
    visible: Boolean,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Isolated State: Terminal live stream simulation without global recomposition cost
    var logList by remember { mutableStateOf(listOf<RuntimeLog>()) }
    
    LaunchedEffect(visible) {
        if (visible) {
            logList = listOf(
                RuntimeLog("00:00:01", "CORE", "Runtime observer attached successfully."),
                RuntimeLog("00:00:02", "EXEC", "Coroutine dispatcher initialized on background thread pool."),
                RuntimeLog("00:00:03", "MEM", "OLED Layout layout boundary cache warmed up.")
            )
            while (true) {
                delay(2500)
                val targetModule = listOf("NET", "SYS", "DB", "SEC", "QUEUE").random()
                val targetMsg = listOf(
                    "Throughput normalized at 48 req/s",
                    "GC clearance cycle completed in 4ms",
                    "KeyStore validation structural check passed.",
                    "Active TCP socket migration executed cleanly.",
                    "Buffer packet optimization step skipped (nominal)"
                ).random()
                
                val currentSeconds = System.currentTimeMillis() / 1000 % 60
                val currentMinutes = System.currentTimeMillis() / 1000 / 60 % 60
                val timeString = String.format("%02d:%02d", currentMinutes, currentSeconds)

                logList = (logList + RuntimeLog(timeString, targetModule, targetMsg)).takeLast(20)
            }
        }
    }

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
        modifier = modifier.fillMaxHeight().fillMaxWidth(0.85f) // Ocupa el 85% lateral derecho
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    // Localized Blur: Aplicado solo a la capa del Drawer (Android 12+)
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
                
                // --- HEADER DE OPERACIONES ---
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
                            text = "OBSERVER_LAYER // CORE_ACTIVE",
                            color = TacticalColors.TextSecondary,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    
                    // Botón físico táctil para cerrar el panel
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

                // --- SECCIÓN 1: LIVE METRICS SYSTEM (Grid Interno Desacoplado) ---
                Text("LIVE METRICS", color = TacticalColors.TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IsolatedTelemetryWidget(modifier = Modifier.weight(1f), title = "ACTIVE_FLOWS", unit = "THREADS") {
                        val flowCount = remember { mutableStateOf(4) }
                        LaunchedEffect(Unit) {
                            while(true) { delay(1800); flowCount.value = Random.nextInt(3, 7) }
                        }
                        Text("${flowCount.value} active", color = TacticalColors.TextPrimary, fontSize = 16.sp, fontFamily = FontFamily.Monospace)
                    }
                    IsolatedTelemetryWidget(modifier = Modifier.weight(1f), title = "MEMORY_PRESSURE", unit = "MB") {
                        val memUsage = remember { mutableStateOf(42.4) }
                        LaunchedEffect(Unit) {
                            while(true) { delay(1200); memUsage.value = 40.0 + Random.nextDouble(1.0, 4.5) }
                        }
                        Text(String.format("%.1f MB", memUsage.value), color = TacticalColors.TextPrimary, fontSize = 16.sp, fontFamily = FontFamily.Monospace)
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IsolatedTelemetryWidget(modifier = Modifier.weight(1f), title = "QUEUE_SIZE", unit = "BUF") {
                        Text("0 / 128", color = TacticalColors.TextSecondary, fontSize = 15.sp, fontFamily = FontFamily.Monospace)
                    }
                    IsolatedTelemetryWidget(modifier = Modifier.weight(1f), title = "FRAME_STABILITY", unit = "FPS") {
                        Text("60 FPS", color = Color(0xFF00FF66), fontSize = 15.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // --- SECCIÓN 2: RUNTIME LOGS TERMINAL (Cinematic Stream) ---
                Text("OBSERVER EVENT LOGS", color = TacticalColors.TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
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
                        reverseLayout = true // Efecto consola donde lo nuevo entra abajo
                    ) {
                        items(logList.reversed()) { log ->
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text("[${log.timestamp}] ", color = TacticalColors.TextSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                Text("${log.level}: ", color = TacticalColors.SystemWarning, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                Text(log.message, color = TacticalColors.TextPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                }
            }
        }
    }
}

// Sub-Componente de telemetría modular desacoplado
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
