package com.cyber.omnigrid.feature.dashboard.presentation

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cyber.omnigrid.core.designsystem.theme.OmniColors

@Composable
fun OmniDashboardScreen(
    onNavigateToLiveExecution: (String) -> Unit,
    onNavigateToSettings: () -> Unit
) {
    // Estado local seguro para alternar el panel de instrumentación avanzada
    var showDebugPanel by remember { mutableStateOf(false) }
    
    Log.d("OMNI_DASHBOARD", "Renderizando capa principal del Dashboard. Modo Debug Interno: $showDebugPanel")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OmniColors.Background)
            .padding(16.dp)
    ) {
        // --- 1. TOP HUD HEADER ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "OMNIGRID // CORE",
                    color = OmniColors.TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "SYS_STATUS: READY",
                    color = OmniColors.AccentGreen,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Botón discreto estilo hardware para alternar telemetría
            Box(
                modifier = Modifier
                    .border(1.dp, if(showDebugPanel) OmniColors.AccentAmber else OmniColors.BorderMuted, RoundedCornerShape(4.dp))
                    .background(if(showDebugPanel) Color(0x1AFF9100) else OmniColors.SurfaceCard)
                    .clickable { showDebugPanel = !showDebugPanel }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (showDebugPanel) "CLOSE_DIAG" else "DIAGNOSTICS",
                    color = if (showDebugPanel) OmniColors.AccentAmber else OmniColors.TextSecondary,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // --- 2. DEBUG OVERLAY PANEL (INLINE) ---
        AnimatedVisibility(
            visible = showDebugPanel,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, OmniColors.AccentAmber, RoundedCornerShape(6.dp))
                    .background(Color(0xFF0F0D0A))
                    .padding(12.dp)
            ) {
                Text("INTERNAL TELEMETRY LOGS", color = OmniColors.AccentAmber, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                Spacer(modifier = Modifier.height(6.dp))
                Text("> [BOOT_OK] Compose UI visual pipeline fully initialized.", color = Color.Gray, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                Text("> [NAV_HOST] Graph attached safely to Screen.Dashboard", color = Color.Gray, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                Text("> [LIFECYCLE] Foreground safe-check validation passed.", color = Color.Gray, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                Text("> [RECOVERY] System state nominal, no outstanding ANR signatures.", color = Color.Gray, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            }
        }

        if (showDebugPanel) Spacer(modifier = Modifier.height(12.dp))

        // --- 3. SYSTEM HEALTH WIDGET ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, OmniColors.BorderMuted, RoundedCornerShape(8.dp))
                .background(OmniColors.SurfaceCard)
                .padding(16.dp)
        ) {
            Text("SYSTEM HEALTH WIDGET", color = OmniColors.TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            
            // Fila de métricas
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("CORE_TEMP", color = Color.Gray, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    Text("34.2 °C", color = OmniColors.TextPrimary, fontSize = 16.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
                Column {
                    Text("CRYPTO_KEY", color = Color.Gray, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    Text("AES_GCM_OK", color = OmniColors.AccentCyan, fontSize = 16.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
                Column {
                    Text("DB_STATUS", color = Color.Gray, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    Text("V1_ACTIVE", color = OmniColors.AccentGreen, fontSize = 16.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("QUICK ACTION MODULES", color = OmniColors.TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))

        // --- 4. CYBERCARDS GRID ---
        Row(modifier = Modifier.fillMaxWidth()) {
            // Card 1: Navegación al Gestor de Payloads
            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(120.dp)
                    .border(1.dp, OmniColors.BorderMuted, RoundedCornerShape(8.dp))
                    .background(OmniColors.SurfaceCard)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { 
                        Log.d("OMNI_NAV", "Accionando CyberCard: Navegar a automatización.")
                        onNavigateToLiveExecution("new_payload") 
                    }
                    .padding(14.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text("▲ AUTOMATION", color = OmniColors.AccentCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                Column {
                    Text("Payloads", color = OmniColors.TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text("Configure scripts", color = Color.Gray, fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Card 2: Placeholder de Configuración o Redes
            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(120.dp)
                    .border(1.dp, OmniColors.BorderMuted, RoundedCornerShape(8.dp))
                    .background(OmniColors.SurfaceCard)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onNavigateToSettings() }
                    .padding(14.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text("[ ] NETWORK", color = OmniColors.TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                Column {
                    Text("Hub Settings", color = OmniColors.TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text("Manage node", color = Color.Gray, fontSize = 11.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- 5. ACTIVITY FEED TERMINAL ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .border(1.dp, OmniColors.BorderMuted, RoundedCornerShape(8.dp))
                .background(OmniColors.SurfaceCard)
                .padding(14.dp)
        ) {
            Text("LIVE EVENTS FEED", color = OmniColors.TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row {
                    Text("[16:18:45] ", color = OmniColors.AccentGreen, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    Text("OmniGrid service listener bounded.", color = OmniColors.TextPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }
                Row {
                    Text("[16:18:40] ", color = OmniColors.AccentCyan, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    Text("Local KeyStore structural check completed.", color = OmniColors.TextPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}
