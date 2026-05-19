package com.cyber.omnigrid.core.os.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cyber.omnigrid.core.os.service.ExecutionSessionManager
import com.cyber.omnigrid.core.os.service.SessionState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class ExecutionRecoveryViewModel : ViewModel() {

    // Se "engancha" automáticamente al flujo del Singleton.
    // Usamos stateIn para que comparta la suscripción y retenga el último estado
    // emitido inmediatamente al recomponer, evitando parpadeos en la UI.
    val sessionState: StateFlow<SessionState> = ExecutionSessionManager.sessionState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ExecutionSessionManager.sessionState.value
        )

    fun injectTestCommand(payload: String = "CMD_DIAGNOSTIC_PING") {
        ExecutionSessionManager.enqueueCommand(payload)
    }

    fun triggerEmergencyPause() {
        ExecutionSessionManager.pauseSession()
    }
    
    fun triggerResume() {
        ExecutionSessionManager.resumeSession()
    }
    
    fun abortExecution() {
        ExecutionSessionManager.stopSession()
    }
}
