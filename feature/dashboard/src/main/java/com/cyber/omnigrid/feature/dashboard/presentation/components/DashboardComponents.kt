package com.cyber.omnigrid.feature.dashboard.presentation.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cyber.omnigrid.core.designsystem.components.CyberCard
import com.cyber.omnigrid.core.designsystem.theme.CyberAccent
import com.cyber.omnigrid.core.designsystem.theme.TextPrimary
import com.cyber.omnigrid.core.designsystem.theme.TextSecondary

@Composable
fun WorkspaceHeader(currentWorkspace: String, onWorkspaceClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = "WORKSPACE ACTIVO", color = TextSecondary, fontSize = 10.sp, letterSpacing = 1.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = currentWorkspace, 
                    color = TextPrimary, 
                    fontSize = 20.sp, 
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown, 
                    contentDescription = "Cambiar", 
                    tint = CyberAccent
                )
            }
        }
        // Acá podría ir el avatar del usuario o un indicador de conexión local
        Box(modifier = Modifier.size(32.dp)) {
            // Placeholder para icono de perfil/settings
        }
    }
}

@Composable
fun SystemStatusWidget() {
    CyberCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text = "SYSTEM METRICS", color = TextSecondary, fontSize = 10.sp)
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatusItem(label = "ENGINE", value = "IDLE", color = TextPrimary)
                // Usamos la lógica de encriptación local para reflejar el estado real
                StatusItem(label = "VAULT (AES/GCM)", value = "SECURED", color = CyberAccent)
                StatusItem(label = "ACTIVE JOBS", value = "0", color = TextPrimary)
            }
        }
    }
}

@Composable
fun StatusItem(label: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Column {
        Text(text = label, color = TextSecondary, fontSize = 10.sp)
        Text(text = value, color = color, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun QuickActionsSection(onActionClick: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(text = "QUICK ACTIONS", color = TextSecondary, fontSize = 10.sp)
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Estos botones disparan acciones directas al Engine
            QuickActionCard("Nuevo Payload", "DuckyScript") { onActionClick("new_payload") }
            QuickActionCard("Escanear LAN", "Network") { onActionClick("scan_lan") }
            QuickActionCard("OSINT Lookup", "Sherlock") { onActionClick("osint") }
        }
    }
}

@Composable
fun QuickActionCard(title: String, subtitle: String, onClick: () -> Unit) {
    CyberCard(
        modifier = Modifier.width(140.dp),
        onClick = onClick
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = title, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text(text = subtitle, color = CyberAccent, fontSize = 10.sp)
        }
    }
}

@Composable
fun RecentActivityList() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(text = "ACTIVITY LOG", color = TextSecondary, fontSize = 10.sp)
        
        // Simulación de registros. Esto luego será un LazyColumn alimentado por Room.
        ActivityRow(action = "Payload Inject: Reverse Shell", time = "Hace 2 min", status = "SUCCESS")
        ActivityRow(action = "Nmap Fast Scan: 192.168.1.0/24", time = "Hace 1 hora", status = "COMPLETED")
        ActivityRow(action = "Workspace Switched", time = "Hace 3 horas", status = "INFO")
    }
}

@Composable
fun ActivityRow(action: String, time: String, status: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = action, color = TextPrimary, fontSize = 13.sp)
            Text(text = time, color = TextSecondary, fontSize = 11.sp)
        }
        Text(
            text = status, 
            color = if (status == "SUCCESS") CyberAccent else TextSecondary, 
            fontSize = 10.sp, 
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ModulesGrid() {
    // Espacio preparado para la grilla de herramientas (Automation, Network, etc)
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        CyberCard(modifier = Modifier.weight(1f)) {
            Text("AUTOMATION", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text("12 Payloads", color = TextSecondary, fontSize = 10.sp)
        }
        CyberCard(modifier = Modifier.weight(1f)) {
            Text("NETWORK", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text("Offline", color = TextSecondary, fontSize = 10.sp)
        }
    }
}
