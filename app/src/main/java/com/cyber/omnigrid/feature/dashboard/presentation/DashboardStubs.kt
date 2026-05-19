package com.cyber.omnigrid.feature.dashboard.presentation

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cyber.omnigrid.core.designsystem.theme.OmniColors
import com.cyber.omnigrid.core.designsystem.theme.tacticalClick
import kotlinx.coroutines.delay

@Composable
fun OmniDashboardScreen(
    onNavigateToLiveExecution: (String) -> Unit,
    onNavigateToSettings: () -> Unit
) {
    var showDebugPanel by remember { mutableStateOf(false) }
    
    // HUD Reactivo: Simulación de Uptime sin bloqueos (Performance first)
    var uptimeSeconds by remember { mutableLongStateOf(0L) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            uptimeSeconds++
        }
    }
    
    // Animación del latido del sistema (Heartbeat)
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alphaPulse by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(OmniColors.SurfaceCard.copy(alpha = 0.3f), OmniColors.Background)
                )
            )
            .padding(16.dp)
            .padding(top = 16.dp) // Espacio para la barra de estado
    ) {
        // --- 1. HUD HEADER (Control Room Style) ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Text(
                    text = "OMNIGRID", // Título geométrico (Sans)
                    color = OmniColors.TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-1).sp
                )
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .alpha(alphaPulse)
                            .clip(RoundedCornerShape(percent = 50))
                            .background(OmniColors.AccentGreen)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "SYS_UPTIME : ${formatUptime(uptimeSeconds)}", // Data técnica en Mono
                        color = OmniColors.TextSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.5.sp
                    )
                }
            }
            
            // Botón de Observabilidad Táctica
            Box(
                modifier = Modifier
                    .border(1.dp, if(showDebugPanel) OmniColors.AccentAmber else OmniColors.BorderMuted, RoundedCornerShape(4.dp))
                    .background(if(showDebugPanel) OmniColors.AccentAmber.copy(alpha = 0.1f) else Color.Transparent)
                    .tacticalClick { showDebugPanel = !showDebugPanel }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if(showDebugPanel) "CLOSE_DIAG" else "DIAGNOSTICS",
                    color = if(showDebugPanel) OmniColors.AccentAmber else OmniColors.TextSecondary,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --- 2. TACTICAL DEBUG CONSOLE ---
        AnimatedVisibility(
            visible = showDebugPanel,
            enter = fadeIn(tween(200)) + expandVertically(spring(stiffness = Spring.StiffnessMediumLow)),
            exit = fadeOut(tween(150)) + shrinkVertically(spring(stiffness = Spring.StiffnessMediumLow))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
                    .border(1.dp, OmniColors.AccentAmber.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                    .background(Color(0xFF0A0700))
                    .padding(12.dp)
            ) {
                Text("TERMINAL // OBSERVER_LAYER", color = OmniColors.AccentAmber, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                Spacer(modifier = Modifier.height(8.dp))
                Text("> Runtime composition ok. Recomps: 0", color = Color.Gray, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                Text("> NavGraph linked to: route=Screen.Dashboard", color = Color.Gray, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                Text("> Hardware keys module: STANDBY", color = Color.Gray, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                Text("> Coroutine Dispatcher OK.", color = Color.Gray, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            }
        }

        Text("MODULES", color = OmniColors.TextSecondary, fontSize = 11.sp, fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(12.dp))

        // --- 3. CYBERCARDS (Hardware Feel) ---
        Row(modifier = Modifier.fillMaxWidth()) {
            CyberCard(
                modifier = Modifier.weight(1f),
                label = "PAYLOAD_MGR",
                title = "Automation",
                subtitle = "Active rules: 0",
                accentColor = OmniColors.AccentCyan,
                onClick = { onNavigateToLiveExecution("new_payload") }
            )
            Spacer(modifier = Modifier.width(12.dp))
            CyberCard(
                modifier = Modifier.weight(1f),
                label = "HUB_LINK",
                title = "Network",
                subtitle = "Disconnected",
                accentColor = OmniColors.TextSecondary,
                onClick = { onNavigateToSettings() }
            )
        }
    }
}

// Sub-componente para limpiar la UI y aislar físicas
@Composable
private fun CyberCard(
    modifier: Modifier = Modifier,
    label: String,
    title: String,
    subtitle: String,
    accentColor: Color,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .height(130.dp)
            .border(1.dp, OmniColors.BorderMuted, RoundedCornerShape(6.dp))
            .clip(RoundedCornerShape(6.dp))
            .background(OmniColors.SurfaceCard)
            .tacticalClick(onClick = onClick)
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = accentColor, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
        Column {
            Text(text = title, color = OmniColors.TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Medium, fontFamily = FontFamily.SansSerif)
            Text(text = subtitle, color = OmniColors.TextSecondary, fontSize = 12.sp, fontFamily = FontFamily.SansSerif)
        }
    }
}

// Helper para el Uptime
private fun formatUptime(seconds: Long): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return String.format("%02d:%02d:%02d", h, m, s)
}
