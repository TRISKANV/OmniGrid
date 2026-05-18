package com.cyber.omnigrid.feature.automation.engine

import com.cyber.omnigrid.feature.automation.domain.engine.EngineState
import com.cyber.omnigrid.feature.automation.domain.engine.PayloadEngine
import com.cyber.omnigrid.feature.automation.domain.model.DuckyAction
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.concurrent.atomic.AtomicBoolean

class MockLoggerEngine : PayloadEngine {
    override val engineName = "Dry Run (Logger)"
    override val isRootRequired = false
    override val isBluetoothRequired = false
    
    private val isCancelled = AtomicBoolean(false)

    override fun execute(actions: List<DuckyAction>): Flow<EngineState> = flow {
        isCancelled.set(false)
        val startTime = System.currentTimeMillis()
        var defaultDelayMs = 0L

        emit(EngineState.Running(0, actions.size, "Inicializando motor virtual..."))
        delay(500)

        for ((index, action) in actions.withIndex()) {
            if (isCancelled.get()) {
                emit(EngineState.Error("Ejecución abortada por el usuario."))
                return@flow
            }

            when (action) {
                is DuckyAction.DefaultDelay -> {
                    defaultDelayMs = action.durationMs
                    emit(EngineState.Running(index + 1, actions.size, "SET DEFAULT DELAY: ${action.durationMs}ms"))
                }
                is DuckyAction.Delay -> {
                    emit(EngineState.Running(index + 1, actions.size, "SLEEP: ${action.durationMs}ms"))
                    delay(action.durationMs)
                }
                is DuckyAction.PressKey -> {
                    val keyString = action.keys.joinToString(" + ")
                    emit(EngineState.Running(index + 1, actions.size, "INJECT KEY: $keyString"))
                    delay(50) // Simular el tiempo de inyección USB/BT
                }
                is DuckyAction.TypeString -> {
                    emit(EngineState.Running(index + 1, actions.size, "TYPING: ${action.text}"))
                    delay(action.text.length * 10L) // Simular velocidad de tipeo
                }
            }

            if (defaultDelayMs > 0 && action !is DuckyAction.DefaultDelay) {
                delay(defaultDelayMs)
            }
        }

        val duration = System.currentTimeMillis() - startTime
        emit(EngineState.Success(duration))
    }

    override fun cancel() {
        isCancelled.set(true)
    }
}
