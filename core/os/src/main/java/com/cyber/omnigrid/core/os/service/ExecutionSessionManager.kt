package com.cyber.omnigrid.core.os.service

import com.cyber.omnigrid.core.os.presentation.LiveMetrics
import com.cyber.omnigrid.core.os.presentation.TransportState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class EngineLifecycleState { IDLE, RUNNING, PAUSED, TERMINATED }

data class SessionState(
    val lifecycleState: EngineLifecycleState = EngineLifecycleState.IDLE,
    val transportState: TransportState = TransportState.IDLE,
    val metrics: LiveMetrics = LiveMetrics(),
    val progress: Float = 0f,
    val currentTaskName: String = "STANDBY"
)

object ExecutionSessionManager {

    private val _sessionState = MutableStateFlow(SessionState())
    val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Default)
    private var executionJob: Job? = null

    fun startSession(taskName: String) {
        if (_sessionState.value.lifecycleState == EngineLifecycleState.RUNNING) return

        executionJob?.cancel()
        _sessionState.update { 
            it.copy(
                lifecycleState = EngineLifecycleState.RUNNING,
                transportState = TransportState.HANDSHAKE,
                currentTaskName = taskName,
                progress = 0f
            )
        }

        runExecutionLoop()
    }

    fun pauseSession() {
        if (_sessionState.value.lifecycleState != EngineLifecycleState.RUNNING) return
        
        executionJob?.cancel()
        _sessionState.update { 
            it.copy(
                lifecycleState = EngineLifecycleState.PAUSED,
                currentTaskName = "PAUSED // SUSPENDED"
            )
        }
    }

    fun resumeSession() {
        if (_sessionState.value.lifecycleState != EngineLifecycleState.PAUSED) return
        _sessionState.update { it.copy(lifecycleState = EngineLifecycleState.RUNNING) }
        runExecutionLoop()
    }

    fun stopSession() {
        executionJob?.cancel()
        _sessionState.update {
            SessionState(
                lifecycleState = EngineLifecycleState.TERMINATED,
                transportState = TransportState.IDLE,
                currentTaskName = "TERMINATED"
            )
        }
    }

    private fun runExecutionLoop() {
        executionJob = scope.launch {
            _sessionState.update { it.copy(transportState = TransportState.CONNECTED) }
            
            var mockCommands = _sessionState.value.metrics.commandsInjected
            var currentProg = _sessionState.value.progress

            while (_sessionState.value.lifecycleState == EngineLifecycleState.RUNNING) {
                delay(800) // Simulación del tick síncrono del motor de ejecución
                mockCommands++
                currentProg += 0.02f
                
                if (currentProg >= 1.0f) {
                    currentProg = 1.0f
                    _sessionState.update {
                        it.copy(
                            lifecycleState = EngineLifecycleState.TERMINATED,
                            currentTaskName = "COMPLETED",
                            progress = 1.0f
                        )
                    }
                    break
                }

                _sessionState.update { state ->
                    state.copy(
                        progress = currentProg,
                        metrics = state.metrics.copy(
                            commandsInjected = mockCommands,
                            txLatencyMs = (3..9).random().toLong(),
                            rxLatencyMs = (2..6).random().toLong()
                        )
                    )
                }
            }
        }
    }
}
