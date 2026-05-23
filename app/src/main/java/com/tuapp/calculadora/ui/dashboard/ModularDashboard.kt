package com.tuapp.calculadora.ui.dashboard

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tuapp.calculadora.core.plugin.RuntimePluginManager
import com.tuapp.calculadora.ui.system.MockSessionStatus
import com.tuapp.calculadora.ui.system.SessionOrchestrator

@Composable
fun ModularDashboard(modifier: Modifier = Modifier) {
    val sessionState by SessionOrchestrator.sessionState.collectAsState()
    val activePlugins by RuntimePluginManager.activePlugins.collectAsState()

    Crossfade(targetState = sessionState == MockSessionStatus.ACTIVE, label = "Boot_Transition") { active ->
        if (!active) {
            BootSequenceOverlay()
        } else {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .background(Color(0xFF030303))
            ) {
                SystemHeader()

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(
                        items = activePlugins,
                        key = { plugin -> plugin.manifest.pluginId }
                    ) { plugin ->
                        Box(modifier = Modifier.fillMaxWidth()) {
                            plugin.RenderWidget()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SystemHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0A0A0A))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "OMNIGRID // TACTICAL RUNTIME",
                color = Color(0xFF00FF66),
                fontSize = 14.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "SESSION: ${SessionOrchestrator.sessionId}",
                color = Color.DarkGray,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }
        Text(
            text = "SYS_ONLINE",
            color = Color(0xFF00E5FF),
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun BootSequenceOverlay() {
    Box(modifier = Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
        Text("WAITING FOR KERNEL BOOTLOADER...", color = Color.Red, fontFamily = FontFamily.Monospace)
    }
}
