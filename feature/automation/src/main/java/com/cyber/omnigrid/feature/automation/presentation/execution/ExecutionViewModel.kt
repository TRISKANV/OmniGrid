package com.cyber.omnigrid.feature.automation.presentation.execution

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cyber.omnigrid.feature.automation.data.hid.MockHidAdapter
import com.cyber.omnigrid.feature.automation.domain.engine.*
import com.cyber.omnigrid.feature.automation.domain.engine.hid.BluetoothHidExecutor
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ExecutionUiState(
    val status: ExecutionStatus = ExecutionStatus.IDLE,
    val logs: List<ExecutionEvent.Log> = emptyList(),
    val currentStep: Int = 0,
    val totalSteps: Int = 0,
    val progressProgress: Float = 0f
)

class ExecutionViewModel(
    // Arquitectura Inyectada: Motor General -> Puente Bluetooth -> Adaptador de Transporte Simulado
    private val engine: OmniExecutionEngine = OmniExecutionEngine(
        parser = DuckyScriptParser(),
        executor = BluetoothHidExecutor(transportAdapter = MockHidAdapter())
    )
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExecutionUiState())
    val uiState: StateFlow<ExecutionUiState> = _uiState.asStateFlow()

    private var executionJob: Job? = null

    fun startExecution(scriptContent: String) {
        executionJob?.cancel()
        _uiState.value = ExecutionUiState() 

        executionJob = viewModelScope.launch {
            engine.execute(scriptContent).collect { event ->
                when (event) {
                    is ExecutionEvent.StatusChange -> {
                        _uiState.update { it.copy(status = event.status) }
                    }
                    is ExecutionEvent.Log -> {
                        _uiState.update { it.copy(logs = it.logs + event) }
                    }
                    is ExecutionEvent.Progress -> {
                        val percentage = if (event.totalSteps > 0) event.currentStep.toFloat() / event.totalSteps else 0f
                        _uiState.update { 
                            it.copy(
                                currentStep = event.currentStep,
                                totalSteps = event.totalSteps,
                                progressProgress = percentage
                            )
                        }
                    }
                }
            }
        }
    }

    fun abortExecution() {
        executionJob?.cancel()
        _uiState.update { it.copy(status = ExecutionStatus.CANCELLED) }
    }

    override fun onCleared() {
        super.onCleared()
        executionJob?.cancel()
    }
}
