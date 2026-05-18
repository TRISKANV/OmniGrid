package com.cyber.omnigrid.feature.automation.domain.engine

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * El orquestador principal. Mantiene el estado seguro, atrapa crashes del hardware
 * y emite la telemetría en tiempo real hacia la UI y Room.
 */
class OmniExecutionEngine(
    private val parser: DuckyScriptParser,
    private val executor: PayloadExecutor
) {
    fun execute(script: String): Flow<ExecutionEvent> = flow {
        emit(ExecutionEvent.StatusChange(ExecutionStatus.CONNECTING))
        emit(ExecutionEvent.Log(LogLevel.INFO, "Iniciando motor: ${executor.executorName}..."))

        try {
            // 1. Fase de Parseo
            emit(ExecutionEvent.Log(LogLevel.INFO, "Analizando sintaxis DuckyScript..."))
            val actions = parser.parse(script)
            val totalSteps = actions.size
            emit(ExecutionEvent.Log(LogLevel.SUCCESS, "Parseo exitoso. Total de operaciones: $totalSteps"))

            // 2. Fase de Conexión (Sandboxed)
            emit(ExecutionEvent.Log(LogLevel.INFO, "Negociando conexión con el Target..."))
            executor.connect()
            emit(ExecutionEvent.StatusChange(ExecutionStatus.RUNNING))
            emit(ExecutionEvent.Log(LogLevel.SUCCESS, "Conexión establecida. Ejecutando Payload."))

            // 3. Fase de Ejecución Interactiva
            actions.forEachIndexed { index, action ->
                emit(ExecutionEvent.Progress(currentStep = index + 1, totalSteps = totalSteps))
                emit(ExecutionEvent.Log(LogLevel.PAYLOAD_STEP, ">> Ejecutando: $action"))
                
                executor.executeAction(action)
                
                // Pequeño delay de seguridad intrínseco para evitar saturar el buffer HID
                delay(10)
            }

            // 4. Finalización exitosa
            emit(ExecutionEvent.StatusChange(ExecutionStatus.SUCCESS))
            emit(ExecutionEvent.Log(LogLevel.SUCCESS, "Payload inyectado con éxito."))

        } catch (e: CancellationException) {
            // Si el usuario presiona "ABORT" en la UI
            emit(ExecutionEvent.StatusChange(ExecutionStatus.CANCELLED))
            emit(ExecutionEvent.Log(LogLevel.WARN, "Ejecución ABORTADA por el operador."))
            throw e
        } catch (e: Exception) {
            // Sandboxing: Cualquier crash del parser o de Bluetooth cae acá
            emit(ExecutionEvent.StatusChange(ExecutionStatus.ERROR))
            emit(ExecutionEvent.Log(LogLevel.ERROR, "CRÍTICO: ${e.message}"))
        } finally {
            // 5. Limpieza garantizada
            emit(ExecutionEvent.Log(LogLevel.INFO, "Liberando interfaces y cerrando canales..."))
            try {
                executor.disconnect()
            } catch (e: Exception) {
                emit(ExecutionEvent.Log(LogLevel.ERROR, "Fallo al desconectar: ${e.message}"))
            }
        }
    }
}
