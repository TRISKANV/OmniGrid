package com.cyber.omnigrid.core.os.service

import com.cyber.omnigrid.core.os.presentation.LiveMetrics
import com.cyber.omnigrid.core.os.presentation.TransportState
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentLinkedQueue

enum class EngineLifecycleState { IDLE, RUNNING, PAUSED, TERMINATED, RECOVERING, ERROR }

// Representación inmutable de un comando a ejecutar en la cola persistente
data class EngineCommand(val id: String, val payload: String, val timestamp: Long = System.currentTimeMillis())

data class SessionState(
    val lifecycleState: EngineLifecycleState = EngineLifecycleState.IDLE,
    val transportState: TransportState = TransportState.IDLE,
    val metrics: LiveMetrics = LiveMetrics(),
    val progress: Float = 0f,
    val currentTaskName: String = "STANDBY",
    val queueSize: Int = 0,
    val uptimeMs: Long = 0L,
    val throughputOps: Float = 0f, // Operaciones por segundo
    val lastError: String? = null
)

object ExecutionSessionManager {

    private val _sessionState = MutableStateFlow(SessionState())
    val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()

    // Manejador global para evitar que una excepción en la ejecución mate el Foreground Service
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        _sessionState.update { 
            it.copy(
                lifecycleState = EngineLifecycleState.ERROR,
                lastError = throwable.localizedMessage ?: "Unknown Engine Crash",
                transportState = TransportState.ERROR
            )
        }
    }

    private val scope = CoroutineScope(Dispatchers.Default + exceptionHandler)
    private var executionJob: Job? = null
    
    // Cola Thread-Safe para resistir inyecciones concurrentes mientras el motor está pausado o recuperándose
    private val commandQueue = ConcurrentLinkedQueue<EngineCommand>()
    private var sessionStartTime: Long = 0L

    fun enqueueCommand(payload: String) {
        commandQueue.add(EngineCommand(id = java.util.UUID.randomUUID().toString(), payload = payload))
        _sessionState.update { it.copy(queueSize = commandQueue.size) }
    }

    fun startSession(taskName: String) {
        if (_sessionState.value.lifecycleState == EngineLifecycleState.RUNNING) return

        executionJob?.cancel()
        sessionStartTime = System.currentTimeMillis()
        
        _sessionState.update { 
            it.copy(
                lifecycleState = EngineLifecycleState.RUNNING,
                transportState = TransportState.HANDSHAKE,
                currentTaskName = taskName,
                progress = 0f,
                lastError = null,
                queueSize = commandQueue.size
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
        if (_sessionState.value.lifecycleState != EngineLifecycleState.PAUSED && 
            _sessionState.value.lifecycleState != EngineLifecycleState.RECOVERING) return
            
        _sessionState.update { it.copy(lifecycleState = EngineLifecycleState.RUNNING) }
        runExecutionLoop()
    }

    fun forceRecovery() {
        // Invocado tras una recreación del servicio por el OS (Process Death Recovery)
        _sessionState.update { it.copy(lifecycleState = EngineLifecycleState.RECOVERING) }
        // Aquí se rehidratarían estados desde Room si la memoria RAM fue borrada por completo
        resumeSession()
    }

    fun stopSession() {
        executionJob?.cancel()
        commandQueue.clear()
        _sessionState.update {
            SessionState(
                lifecycleState = EngineLifecycleState.TERMINATED,
                transportState = TransportState.IDLE,
                currentTaskName = "TERMINATED",
                queueSize = 0,
                uptimeMs = 0L
            )
        }
    }

    private fun runExecutionLoop() {
        executionJob = scope.launch {
            _sessionState.update { it.copy(transportState = TransportState.CONNECTED) }
            
            var processedCommands = _sessionState.value.metrics.commandsInjected
            var currentProg = _sessionState.value.progress

            while (_sessionState.value.lifecycleState == EngineLifecycleState.RUNNING) {
                val cycleStartTime = System.currentTimeMillis()
                
                // Procesar cola real si hay comandos, de lo contrario simular tick de *keep-alive*
                val command = commandQueue.poll()
                if (command != null) {
                    delay(150) // Simulación de I/O real por comando
                    processedCommands++
                } else {
                    delay(800) // Latencia de polling si la cola está vacía
                    currentProg += 0.02f // Simulación de progreso de tarea base
                }
                
                val currentUptime = System.currentTimeMillis() - sessionStartTime
                val throughput = if (currentUptime > 0) (processedCommands.toFloat() / (currentUptime / 1000f)) else 0f

                if (currentProg >= 1.0f && commandQueue.isEmpty()) {
                    _sessionState.update {
                        it.copy(
                            lifecycleState = EngineLifecycleState.TERMINATED,
                            currentTaskName = "COMPLETED",
                            progress = 1.0f,
                            queueSize = 0
                        )
                    }
                    break
                }

                _sessionState.update { state ->
                    state.copy(
                        progress = currentProg.coerceAtMost(1.0f),
                        queueSize = commandQueue.size,
                        uptimeMs = currentUptime,
                        throughputOps = throughput,
                        metrics = state.metrics.copy(
                            commandsInjected = processedCommands,
                            txLatencyMs = (System.currentTimeMillis() - cycleStartTime), // Latencia real del ciclo
                            rxLatencyMs = (2..6).random().toLong()
                        )
                    )
                }
            }
        }
    }
}
