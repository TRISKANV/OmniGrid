package com.cyber.omnigrid.feature.automation.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cyber.omnigrid.core.designsystem.components.CyberCard
import com.cyber.omnigrid.core.designsystem.components.CyberHudProgress
import com.cyber.omnigrid.core.designsystem.components.LiveTerminalLog
import com.cyber.omnigrid.core.designsystem.theme.CyberAccent
import com.cyber.omnigrid.core.designsystem.theme.TrueBlack
import com.cyber.omnigrid.feature.automation.domain.engine.EngineState
import com.cyber.omnigrid.feature.automation.domain.parser.DuckyScriptParser
import com.cyber.omnigrid.feature.automation.engine.MockLoggerEngine
import kotlinx.coroutines.launch

@Composable
fun LiveExecutionScreen() {
    val scope = rememberCoroutineScope()
    
    // Instanciamos el core desacoplado
    val parser = remember { DuckyScriptParser() }
    val engine = remember { MockLoggerEngine() }

    // Estados de UI reactivos
    var engineState by remember { mutableStateOf<EngineState>(EngineState.Idle) }
    val logHistory = remember { mutableStateListOf<String>() }

    // Un script de prueba duro en el código para validar la UI
    val sampleDuckyScript = """
        REM Iniciando ataque de automatización experimental
        DEFAULTDELAY 200
        GUI r
        DELAY 500
        STRING powershell -NoProfile -ExecutionPolicy Bypass
        ENTER
        DELAY 700
        STRING Write-Host 'OmniGrid Core Loaded Successfully' -ForegroundColor Cyan
        ENTER
    """.trimIndent()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TrueBlack)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- CABECERA HUD ---
        Text(
            text = "// OMNIGRID_EXECUTION_DECK",
            color = CyberAccent,
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // --- DASHBOARD DE ESTADO (CyberCard) ---
        CyberCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = when (engineState) {
                            is EngineState.Idle -> "ESTADO: EN ESPERA"
                            is EngineState.Running -> "ESTADO: INYECTANDO PAYLOAD"
                            is EngineState.Success -> "ESTADO: INYECCIÓN COMPLETADA"
                            is EngineState.Error -> "ESTADO: ERROR DE SISTEMA"
                        },
                        fontSize = 16.sp
                    )
                    
                    if (engineState is EngineState.Running) {
                        val run = engineState as EngineState.Running
                        Text(
                            text = "Acción ${run.currentActionIndex} de ${run.totalActions}",
                            fontSize = 12.sp
                        )
                    }
                }

                // Renderizado condicional del indicador HUD según el estado del Flow
                if (engineState is EngineState.Running) {
                    val run = engineState as EngineState.Running
                    val progressFloat = run.currentActionIndex.toFloat() / run.totalActions.toFloat()
                    CyberHudProgress(progress = progressFloat)
                } else {
                    CyberHudProgress(progress = 0f)
                }
            }
        }

        // --- PANEL DE TERMINAL EN VIVO (Muestra la tubería de datos asíncrona) ---
        CyberCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "LIVE OUTPUT LOGS", fontSize = 12.sp)
                LiveTerminalLog(logs = logHistory)
            }
        }

        // --- PANEL DE CONTROL DE OPERACIONES ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = {
                    scope.launch {
                        logHistory.clear()
                        val actions = parser.parse(sampleDuckyScript)
                        
                        // Recolectamos el Flow reactivo del motor e impactamos la UI
                        engine.execute(actions).collect { state ->
                            engineState = state
                            if (state is EngineState.Running && state.log != null) {
                                logHistory.add(state.log)
                            } else if (state is EngineState.Success) {
                                logHistory.add(">> Ejecución finalizada con éxito en ${state.durationMs}ms")
                            } else if (state is EngineState.Error) {
                                logHistory.add("!! FATAL: ${state.message}")
                            }
                        }
                    }
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = CyberAccent)
            ) {
                Text("DISPARAR PAYLOAD", color = TrueBlack)
            }

            Button(
                onClick = { engine.cancel() },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = TrueBlack),
                modifier = Modifier.border(1.dp, CyberAccent)
            ) {
                Text("ABORTAR", color = CyberAccent)
            }
        }
    }
}
