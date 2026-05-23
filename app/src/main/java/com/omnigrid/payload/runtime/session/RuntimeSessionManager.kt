package com.omnigrid.payload.runtime.session

import com.omnigrid.payload.domain.model.*
import com.omnigrid.payload.domain.repository.PayloadRepository
import com.omnigrid.payload.runtime.engine.DuckyRuntimeEngine
import com.omnigrid.payload.runtime.events.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class RuntimeSessionManager(
    private val engine: DuckyRuntimeEngine,
    private val repository: PayloadRepository,
    private val eventDispatcher: PayloadEventDispatcher
) {
    private val managerScope = CoroutineScope(
        SupervisorJob() + Dispatchers.IO + CoroutineName("SessionManager")
    )

    // Cola de sesiones pendientes
    private val executionQueue = MutableStateFlow<List<String>>(emptyList())
    val queue: StateFlow<List<String>> = executionQueue.asStateFlow()

    init {
        // Escuchar todos los eventos del engine y persistirlos
        observeEngineEvents()
    }

    // ── API pública ───────────────────────────────────────────────────────────

    suspend fun enqueueExecution(payloadId: String): String {
        val payload = repository.getPayloadById(payloadId) 
            ?: throw IllegalArgumentException("Payload $payloadId not found")

        val session = PayloadSession(
            payloadId = payloadId,
            payloadName = payload.name,
            state = ExecutionState.QUEUED
        )

        repository.insertSession(session)
        repository.incrementExecutionCount(payloadId)

        executionQueue.update { it + session.sessionId }

        managerScope.launch {
            executeSession(session)
        }

        return session.sessionId
    }

    fun cancel(sessionId: String) {
        engine.cancel(sessionId)
        managerScope.launch {
            repository.updateSessionState(
                sessionId = sessionId,
                state = ExecutionState.CANCELLED,
                endedAt = System.currentTimeMillis()
            )
        }
        executionQueue.update { it - sessionId }
    }

    fun pause(sessionId: String) {
        managerScope.launch {
            repository.updateSessionState(sessionId, ExecutionState.PAUSED)
        }
    }

    fun observeSession(sessionId: String): Flow<PayloadSession?> =
        repository.observeSessionById(sessionId)

    fun observeActiveSessions(): Flow<List<PayloadSession>> =
        repository.observeActiveSessions()

    // ── Internals ─────────────────────────────────────────────────────────────

    private suspend fun executeSession(session: PayloadSession) {
        val payload = repository.getPayloadById(session.payloadId) ?: run {
            repository.updateSessionState(session.sessionId, ExecutionState.FAILED, System.currentTimeMillis())
            return
        }

        repository.updateSessionState(session.sessionId, ExecutionState.INITIALIZING)

        engine.execute(session, payload.script)

        executionQueue.update { it - session.sessionId }
    }

    private fun observeEngineEvents() {
        managerScope.launch {
            engine.events.collect { event ->
                processEvent(event)
            }
        }
    }

    private suspend fun processEvent(event: PayloadRuntimeEvent) {
        val sessionId = event.sessionId

        // 1. Persistir log en Room
        val log = SessionLog(
            timestamp = event.timestamp,
            level = event.toLogLevel(),
            tag = event::class.simpleName ?: "Event",
            message = event.toLogMessage(),
            actionIndex = when (event) {
                is PayloadRuntimeEvent.ActionStarted -> event.actionIndex
                is PayloadRuntimeEvent.ActionCompleted -> event.actionIndex
                is PayloadRuntimeEvent.ActionFailed -> event.actionIndex
                else -> null
            }
        )
        repository.appendSessionLog(sessionId, log)

        // 2. Actualizar estado de sesión según evento
        when (event) {
            is PayloadRuntimeEvent.SessionStateChanged -> {
                repository.updateSessionState(sessionId, event.state)
            }
            is PayloadRuntimeEvent.ActionCompleted -> {
                repository.updateSessionProgress(sessionId, event.completed, 0)
            }
            is PayloadRuntimeEvent.ActionFailed -> {
                val session = repository.getSessionById(sessionId)
                session?.let {
                    repository.updateSessionProgress(sessionId, it.completedActions, it.failedActions + 1)
                }
            }
            is PayloadRuntimeEvent.TransportStateChanged -> {
                repository.updateTransportState(sessionId, event.state)
            }
            is PayloadRuntimeEvent.SessionCompleted -> {
                repository.updateSessionState(sessionId, ExecutionState.COMPLETED, event.timestamp)
                repository.updateSessionMetrics(sessionId, event.metrics)
            }
            is PayloadRuntimeEvent.SessionFailed -> {
                repository.updateSessionState(sessionId, ExecutionState.FAILED, event.timestamp)
            }
            else -> { /* Warning, ParseCompleted: solo log local */ }
        }

        // 3. Forward al CoreEventBus del Runtime OS
        eventDispatcher.dispatch(event)
    }
}
