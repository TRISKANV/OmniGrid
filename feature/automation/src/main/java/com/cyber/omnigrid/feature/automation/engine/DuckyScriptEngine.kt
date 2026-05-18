package com.cyber.omnigrid.feature.automation.engine

import com.cyber.omnigrid.core.executor.CyberTool
import com.cyber.omnigrid.core.executor.ToolExecutionState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

// Modelos específicos para esta herramienta
data class PayloadConfig(val script: String, val delayMs: Long = 1000L)
data class ExecutionReport(val linesExecuted: Int, val durationMs: Long)

class DuckyScriptEngine : CyberTool<PayloadConfig, ExecutionReport> {
    override val id = "rucky_duckyscript_v1"
    override val name = "HID Payload Injector"
    override val isRootRequired = true // Rucky necesita escribir en /dev/hidg0

    override fun execute(config: PayloadConfig): Flow<ToolExecutionState<ExecutionReport>> = flow {
        emit(ToolExecutionState.Running(logLine = "Iniciando motor HID..."))
        delay(config.delayMs) // Delay inicial de seguridad
        
        val lines = config.script.lines()
        var executed = 0
        val startTime = System.currentTimeMillis()

        try {
            lines.forEachIndexed { index, line ->
                if (line.isNotBlank() && !line.startsWith("REM")) { // REM es comentario en DuckyScript
                    emit(ToolExecutionState.Running(
                        progress = (index + 1) / lines.size.toFloat(),
                        logLine = "Ejecutando: $line"
                    ))
                    
                    // ACÁ VA LA MAGIA DE RUCKY: Parsear el 'line' a Keycodes y mandar a /dev/hidg0
                    // Ej: writeToHidDevice(parseLine(line))
                    delay(50) // Simulación de tipeo / espera entre comandos
                    executed++
                }
            }
            
            val duration = System.currentTimeMillis() - startTime
            emit(ToolExecutionState.Success(ExecutionReport(executed, duration)))
            
        } catch (e: Exception) {
            emit(ToolExecutionState.Error("Fallo en la inyección HID al kernel", e))
        }
    }

    override fun stop() {
        // Lógica para abortar la escritura en /dev/hidg0
    }
}
