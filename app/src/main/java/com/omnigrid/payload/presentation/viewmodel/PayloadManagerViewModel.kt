package com.omnigrid.payload.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omnigrid.payload.domain.model.*
import com.omnigrid.payload.domain.usecase.*
import com.omnigrid.payload.runtime.session.RuntimeSessionManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PayloadManagerViewModel(
    private val getPayloads: GetPayloadsUseCase,
    private val createPayload: CreatePayloadUseCase,
    private val updatePayload: UpdatePayloadUseCase,
    private val deletePayload: DeletePayloadUseCase,
    private val executePayload: ExecutePayloadUseCase,
    private val manageSession: ManageSessionUseCase,
    private val sessionManager: RuntimeSessionManager
) : ViewModel() {

    private val _filterState = MutableStateFlow(FilterState())
    val filterState: StateFlow<FilterState> = _filterState.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val payloads: StateFlow<List<Payload>> = _filterState
        .flatMapLatest { filter ->
            when {
                filter.query.isNotBlank() -> getPayloads.search(filter.query)
                filter.onlyFavorites -> getPayloads.favorites()
                filter.category != null -> getPayloads.byCategory(filter.category)
                filter.tag != null -> getPayloads.byTag(filter.tag)
                else -> getPayloads.all()
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeSessions: StateFlow<List<PayloadSession>> = sessionManager
        .observeActiveSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dashboardStats: StateFlow<DashboardStats> = manageSession
        .observeDashboardStats()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            DashboardStats(0, 0, 0f, 0)
        )

    private val _uiEvent = MutableSharedFlow<ManagerUiEvent>()
    val uiEvent: SharedFlow<ManagerUiEvent> = _uiEvent.asSharedFlow()

    private val _selectedPayload = MutableStateFlow<Payload?>(null)
    val selectedPayload: StateFlow<Payload?> = _selectedPayload.asStateFlow()

    fun setQuery(query: String) {
        _filterState.update { it.copy(query = query) }
    }

    fun setCategory(category: PayloadCategory?) {
        _filterState.update { it.copy(category = category, tag = null, onlyFavorites = false) }
    }

    fun setTag(tag: String?) {
        _filterState.update { it.copy(tag = tag, category = null, onlyFavorites = false) }
    }

    fun setFavoritesOnly(only: Boolean) {
        _filterState.update { it.copy(onlyFavorites = only, category = null, tag = null) }
    }

    fun selectPayload(payload: Payload?) {
        _selectedPayload.value = payload
    }

    fun createPayload(
        name: String,
        script: String,
        description: String = "",
        category: PayloadCategory = PayloadCategory.UNCATEGORIZED,
        tags: List<String> = emptyList()
    ) {
        viewModelScope.launch {
            runCatching {
                createPayload.invoke(name, script, description, category, tags)
            }.onSuccess {
                _uiEvent.emit(ManagerUiEvent.PayloadCreated(it.id))
            }.onFailure { error ->
                _uiEvent.emit(ManagerUiEvent.Error(error.message ?: "Create failed"))
            }
        }
    }

    fun executePayload(payloadId: String) {
        viewModelScope.launch {
            runCatching {
                sessionManager.enqueueExecution(payloadId)
            }.onSuccess { sessionId ->
                _uiEvent.emit(ManagerUiEvent.ExecutionStarted(sessionId))
            }.onFailure { error ->
                _uiEvent.emit(ManagerUiEvent.Error(error.message ?: "Execution failed"))
            }
        }
    }

    fun deletePayload(payloadId: String) {
        viewModelScope.launch {
            runCatching {
                deletePayload.invoke(payloadId)
            }.onSuccess {
                _uiEvent.emit(ManagerUiEvent.PayloadDeleted(payloadId))
            }.onFailure { error ->
                _uiEvent.emit(ManagerUiEvent.Error(error.message ?: "Delete failed"))
            }
        }
    }

    fun toggleFavorite(payloadId: String) {
        // Optimistic update vía repository
    }

    fun cancelSession(sessionId: String) {
        sessionManager.cancel(sessionId)
    }
}

data class FilterState(
    val query: String = "",
    val category: PayloadCategory? = null,
    val tag: String? = null,
    val onlyFavorites: Boolean = false
)

sealed class ManagerUiEvent {
    data class PayloadCreated(val id: String) : ManagerUiEvent()
    data class PayloadDeleted(val id: String) : ManagerUiEvent()
    data class ExecutionStarted(val sessionId: String) : ManagerUiEvent()
    data class Error(val message: String) : ManagerUiEvent()
}
