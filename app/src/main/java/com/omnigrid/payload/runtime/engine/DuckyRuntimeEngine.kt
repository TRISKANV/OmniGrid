package com.omnigrid.payload.runtime.engine

import com.omnigrid.payload.domain.model.*
import com.omnigrid.payload.runtime.events.PayloadRuntimeEvent
import com.omnigrid.payload.runtime.transport.TransportLayer
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class DuckyRuntimeEngine(
    private val parser: DuckyScriptParser,
    private val transport: TransportLayer
) {
    private val _events = MutableSharedFlow<PayloadRuntimeEvent>(replay = 0, extraBufferCapacity = 128)
    val events: SharedFlow<PayloadRuntimeEvent> = _events.asSharedFlow()

    private val engineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default + CoroutineName("DuckyEngine"))
    private val activeJobs = mutableMapOf<String, Job>()

    fun execute(session: PayloadSession, script: String): Job {
        val job = engineScope.launch {
            runCatching {
                runSession(session, script)
            }.onFailure { throwable ->
                if (throwable !is CancellationException) {
                    emit(PayloadRuntimeEvent.SessionFailed(session.sessionId, throwable.message ?: "Unknown engine error", System.currentTimeMillis()))
                }
            }
        }
        activeJobs[session.sessionId] = job
        job.invokeOnCompletion { activeJobs.remove(session.sessionId) }
        return job
    }

    fun cancel(sessionId: String) {
        activeJobs[sessionId]?.cancel(CancellationException("Session $sessionId cancelled by user"))
    }

    fun cancelAll() {
        activeJobs.values.forEach { it.cancel() }
        activeJobs.clear()
    }

    fun isRunning(sessionId: String): Boolean = activeJobs[sessionId]?.isActive == true

    private suspend fun runSession(session: PayloadSession, script: String) {
        val sessionId = session.sessionId
        val startMs = System.currentTimeMillis()

        emit(PayloadRuntimeEvent.SessionStateChanged(sessionId, ExecutionState.INITIALIZING))

        val parseResult = parser.parse(script)
        parseResult.warnings.forEach { warning -> emit(PayloadRuntimeEvent.Warning(sessionId, warning, System.currentTimeMillis())) }
        emit(PayloadRuntimeEvent.ParseCompleted(sessionId, parseResult.actionCount, parseResult.warnings, System.currentTimeMillis()))

        val transportConnected = transport.connect()
        emit(PayloadRuntimeEvent.TransportStateChanged(sessionId, if (transportConnected) TransportState.CONNECTED else TransportState.ERROR, System.currentTimeMillis()))

        if (!transportConnected) {
            emit(PayloadRuntimeEvent.SessionFailed(sessionId, "Transport failed to connect", System.currentTimeMillis()))
            return
        }

        val initMs = System.currentTimeMillis() - startMs
        emit(PayloadRuntimeEvent.SessionStateChanged(sessionId, ExecutionState.RUNNING))

        var completed = 0
        var failed = 0
        val executionTimes = mutableListOf<Long>()
        val executableActions = parseResult.actions.filter { it !is DuckyAction.Rem && it !is DuckyAction.DefaultDelay }

        for (action in executableActions) {
            ensureActive()
            val actionStart = System.currentTimeMillis()
            emit(PayloadRuntimeEvent.ActionStarted(sessionId, action.index, action::class.simpleName ?: "Unknown", actionStart))

            val result = executeAction(action)
            val latency = System.currentTimeMillis() - actionStart
            executionTimes.add(latency)

            when (result) {
                is ExecutionResult.Success -> {
                    completed++
                    emit(PayloadRuntimeEvent.ActionCompleted(sessionId, action.index, latency, completed, executableActions.size, System.currentTimeMillis()))
                }
                is ExecutionResult.Failure -> {
                    failed++
                    emit(PayloadRuntimeEvent.ActionFailed(sessionId, action.index, result.reason, result.isRecoverable, System.currentTimeMillis()))
                    if (!result.isRecoverable) {
                        emit(PayloadRuntimeEvent.SessionFailed(sessionId, "Non-recoverable action failure at index ${action.index}: ${result.reason}", System.currentTimeMillis()))
                        transport.disconnect()
                        return
                    }
                }
                is ExecutionResult.Warning -> {
                    emit(PayloadRuntimeEvent.Warning(sessionId, result.message, System.currentTimeMillis()))
                }
            }
        }

        transport.disconnect()
        emit(PayloadRuntimeEvent.TransportStateChanged(sessionId, TransportState.DISCONNECTED, System.currentTimeMillis()))

        val metrics = ExecutionMetrics(
            initTimeMs = initMs,
            totalExecutionMs = System.currentTimeMillis() - startMs,
            avgActionLatencyMs = if (executionTimes.isEmpty()) 0L else executionTimes.average().toLong(),
            peakActionLatencyMs = executionTimes.maxOrNull() ?: 0L,
            bytesSent = transport.bytesSent()
        )
        emit(PayloadRuntimeEvent.SessionCompleted(sessionId, metrics, completed, failed, System.currentTimeMillis()))
    }

    private suspend fun executeAction(action: DuckyAction): ExecutionResult {
        return when (action) {
            is DuckyAction.Delay -> { delay(action.millis); ExecutionResult.Success(action.index, "Delay", action.millis) }
            is DuckyAction.KeyPress -> { transport.sendKeyPress(action.key, action.modifiers); ExecutionResult.Success(action.index, "KeyPress", 0L) }
            is DuckyAction.StringType -> { transport.sendString(action.text); ExecutionResult.Success(action.index, "StringType", 0L) }
            is DuckyAction.StringTypeLine -> { transport.sendString(action.text); transport.sendKeyPress("ENTER", emptyList()); ExecutionResult.Success(action.index, "StringTypeLine", 0L) }
            is DuckyAction.Led -> { ExecutionResult.Success(action.index, "Led", 0L) }
            is DuckyAction.Unknown -> { ExecutionResult.Warning(action.index, "Skipped unknown action: ${action.raw}") }
            else -> { ExecutionResult.Success(action.index, action::class.simpleName ?: "Action", 0L) }
        }
    }

    private suspend fun emit(event: PayloadRuntimeEvent) { _events.emit(event) }
}

sealed class ExecutionResult {
    data class Success(val actionIndex: Int, val actionType: String, val latencyMs: Long) : ExecutionResult()
    data class Failure(val actionIndex: Int, val actionType: String, val reason: String, val isRecoverable: Boolean = false) : ExecutionResult()
    data class Warning(val actionIndex: Int, val message: String) : ExecutionResult()
}
