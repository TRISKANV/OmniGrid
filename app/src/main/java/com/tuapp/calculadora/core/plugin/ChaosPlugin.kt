package com.tuapp.calculadora.core.plugin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tuapp.calculadora.core.CoreEventBus
import com.tuapp.calculadora.core.SystemEvent
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ChaosPlugin : OmniPlugin {

    override val manifest = PluginManifest(
        pluginId = "core.chaos.monkey",
        version = "1.0.0",
        capabilities = setOf(PluginCapability.STRESS_TESTING, PluginCapability.UI_DASHBOARD_WIDGET),
        priority = 50 
    )

    private val _state = MutableStateFlow(PluginState.IDLE)
    override val state: StateFlow<PluginState> = _state.asStateFlow()

    // Este scope simula el hilo de fondo del plugin.
    // Usamos el handler inyectado por el Kernel invisiblemente (gracias a la estructura de corrutinas estructuradas),
    // pero para simular fallos asíncronos reales que escapen a Compose, lanzaremos tareas en Dispatchers.IO
    private var workScope: CoroutineScope? = null

    override suspend fun initialize() {
        _state.value = PluginState.INITIALIZING
    }

    override suspend fun start() {
        _state.value = PluginState.RUNNING
        workScope = CoroutineScope(Dispatchers.IO)
    }

    override suspend fun stop() {
        workScope?.cancel()
        _state.value = PluginState.DISABLED
    }

    override suspend fun recover() {
        _state.value = PluginState.RECOVERING
        workScope?.cancel()
        delay(1000) // Simula limpieza
        _state.value = PluginState.IDLE
    }

    // --- MÉTODOS DE ESTRÉS ---

    private fun triggerException() {
        // Lanzamos la excepción en un hilo de fondo. El Sandbox Layer del Manager DEBE atraparlo.
        workScope?.launch {
            delay(200)
            throw RuntimeException("SIMULATED FATAL COROUTINE CRASH")
        }
    }

    private fun triggerEventSpam() {
        workScope?.launch {
            for (i in 1..500) {
                CoreEventBus.publish(SystemEvent("CHAOS_SPAM_PACKET_$i"))
                delay(2) // Spam masivo al bus
            }
        }
    }

    @Composable
    override fun RenderWidget() {
        val currentState by _state.collectAsState()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF2A0000))
                .padding(12.dp)
        ) {
            Text(
                text = "CHAOS_ENGINE :: [${manifest.pluginId}]",
                color = Color.Red,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "STATE: $currentState",
                color = Color.Gray,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )
            
            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { triggerException() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("TRIGGER CRASH", fontSize = 10.sp, color = Color.White)
                }
                
                Button(
                    onClick = { triggerEventSpam() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9900))
                ) {
                    Text("EVENT SPAM", fontSize = 10.sp, color = Color.Black)
                }
            }
        }
    }
}
