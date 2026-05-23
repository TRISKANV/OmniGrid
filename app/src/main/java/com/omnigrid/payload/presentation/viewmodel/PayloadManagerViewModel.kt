package com.omnigrid.payload.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omnigrid.payload.domain.model.*
import com.omnigrid.payload.domain.repository.PayloadRepository
import com.omnigrid.payload.runtime.engine.DuckyRuntimeEngine
import com.omnigrid.payload.runtime.events.PayloadRuntimeEvent
import com.omnigrid.payload.runtime.session.RuntimeSessionManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PayloadTerminalViewModel(
    private val sessionManager: RuntimeSessionManager,
    private val repository: PayloadRepository,
    private val engine: DuckyRuntimeEngine,
    val sessionId: String? = null
) : ViewModel() {

    val session: StateFlow<PayloadSession?> = sessionId?.let { id ->
        repository.observeSessionById(id)
    }?.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null) ?: MutableStateFlow(null)

    private val _liveEvents = MutableStateFlow<List<LiveTerminalEntry>>(emptyList())
    val liveEvents: StateFlow<List<LiveTerminalEntry>> = _liveEvents.asStateFlow()

    private val _autoScroll = MutableStateFlow(true)
    val autoScroll: StateFlow<Boolean> = _autoScroll.asStateFlow()

    val liveMetrics: StateFlow<LiveMetrics> = session.map { s ->
        if (s == null) LiveMetrics()
        else LiveMetrics(
            progress = s.progress, state = s.state, completedActions = s.completedActions,
            failedActions = s.failedActions, totalActions = s.totalActions,
            durationMs = s.duration, transportState = s.transportState, warningCount = s.warnings.size
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LiveMetrics())

    init { observeEngineEvents() }

    private fun observeEngineEvents() {
        viewModelScope.launch {
            engine.events.filter { sessionId == null || it.sessionId == sessionId }.collect { event ->
                val entry = LiveTerminalEntry(
                    timestamp = event.timestamp, level = event.toLogLevelLabel(),
                    message = event.toDisplayMessage(), category = event.toCategory(), isHighlight = event.isHighlightEvent()
                )
                _liveEvents.update { current -> (current + entry).takeLast(500) }
            }
        }
    }

    fun cancelSession() { sessionId?.let { sessionManager.cancel(it) } }
    fun clearLiveEvents() { _liveEvents.value = emptyList() }
    fun setAutoScroll(enabled: Boolean) { _autoScroll.value = enabled }

    private fun PayloadRuntimeEvent.toLogLevelLabel(): String = when (this) {
        is PayloadRuntimeEvent.SessionFailed -> "CRIT"
        is PayloadRuntimeEvent.ActionFailed -> "ERR "
        is PayloadRuntimeEvent.Warning -> "WARN"
        is PayloadRuntimeEvent.SessionCompleted -> "INFO"
        is PayloadRuntimeEvent.ActionCompleted -> "OK  "
        else -> "DBG "
    }

    private fun PayloadRuntimeEvent.toDisplayMessage(): String = when (this) {
        is PayloadRuntimeEvent.SessionStateChanged -> "SESSION  ${state.name.padEnd(12)} ›  ${sessionId.take(8)}"
        is PayloadRuntimeEvent.SessionCompleted -> "COMPLETE ${completedActions}/${completedActions + failedActions} actions  ${metrics.totalExecutionMs}ms"
        is PayloadRuntimeEvent.SessionFailed -> "FAILED   $reason"
        is PayloadRuntimeEvent.ParseCompleted -> "PARSED   $actionCount actions  ${warnings.size} warnings"
        is PayloadRuntimeEvent.ActionStarted -> "ACTION   [$actionIndex] $actionType"
        is PayloadRuntimeEvent.ActionCompleted -> "DONE     [$actionIndex] ${latencyMs}ms  [$completed/$total]"
        is PayloadRuntimeEvent.ActionFailed -> "FAIL     [$actionIndex] $reason"
        is PayloadRuntimeEvent.TransportStateChanged -> "TRANSPORT ${state.name}"
        is PayloadRuntimeEvent.Warning -> "WARNING  $message"
    }

    private fun PayloadRuntimeEvent.toCategory(): TerminalCategory = when (this) {
        is PayloadRuntimeEvent.SessionStateChanged, is PayloadRuntimeEvent.SessionCompleted, is PayloadRuntimeEvent.SessionFailed -> TerminalCategory.SESSION
        is PayloadRuntimeEvent.ActionStarted, is PayloadRuntimeEvent.ActionCompleted, is PayloadRuntimeEvent.ActionFailed -> TerminalCategory.ACTION
        is PayloadRuntimeEvent.TransportStateChanged -> TerminalCategory.TRANSPORT
        is PayloadRuntimeEvent.ParseCompleted -> TerminalCategory.PARSE
        is PayloadRuntimeEvent.Warning -> TerminalCategory.WARNING
    }

    private fun PayloadRuntimeEvent.isHighlightEvent(): Boolean = this is PayloadRuntimeEvent.SessionCompleted || this is PayloadRuntimeEvent.SessionFailed || this is PayloadRuntimeEvent.SessionStateChanged
}

data class LiveTerminalEntry(val timestamp: Long, val level: String, val message: String, val category: TerminalCategory, val isHighlight: Boolean = false)
enum class TerminalCategory { SESSION, ACTION, TRANSPORT, PARSE, WARNING }
data class LiveMetrics(
    val progress: Float = 0f, val state: ExecutionState = ExecutionState.QUEUED, val completedActions: Int = 0,
    val failedActions: Int = 0, val totalActions: Int = 0, val durationMs: Long = 0L,
    val transportState: TransportState = TransportState.DISCONNECTED, val warningCount: Int = 0
)
