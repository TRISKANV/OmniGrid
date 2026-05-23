package com.omnigrid.payload.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omnigrid.payload.domain.model.PayloadCategory
import com.omnigrid.payload.domain.repository.PayloadRepository
import com.omnigrid.payload.domain.usecase.CreatePayloadUseCase
import com.omnigrid.payload.domain.usecase.UpdatePayloadUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PayloadEditorViewModel(
    private val createPayload: CreatePayloadUseCase,
    private val updatePayload: UpdatePayloadUseCase,
    private val repository: PayloadRepository,
    private val payloadId: String? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    private val _saved = MutableSharedFlow<Unit>()
    val saved: SharedFlow<Unit> = _saved.asSharedFlow()

    init { payloadId?.let { loadPayload(it) } }

    private fun loadPayload(id: String) {
        viewModelScope.launch {
            val payload = repository.getPayloadById(id) ?: return@launch
            _uiState.update {
                it.copy(
                    isEditMode = true, name = payload.name, description = payload.description,
                    script = payload.script, category = payload.category, tagsRaw = payload.tags.joinToString(", ")
                )
            }
        }
    }

    fun setName(name: String) = _uiState.update { it.copy(name = name, error = null) }
    fun setDescription(desc: String) = _uiState.update { it.copy(description = desc) }
    fun setCategory(cat: PayloadCategory) = _uiState.update { it.copy(category = cat) }
    fun setTagsRaw(tags: String) = _uiState.update { it.copy(tagsRaw = tags) }

    fun save(script: String) {
        val state = _uiState.value
        if (state.name.isBlank()) { _uiState.update { it.copy(error = "Name is required") }; return }
        if (script.isBlank()) { _uiState.update { it.copy(error = "Script cannot be empty") }; return }

        val tags = state.tagsRaw.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        _uiState.update { it.copy(isSaving = true, error = null) }

        viewModelScope.launch {
            runCatching {
                if (state.isEditMode && payloadId != null) {
                    val existing = repository.getPayloadById(payloadId)!!
                    updatePayload(existing.copy(name = state.name, description = state.description, script = script, category = state.category, tags = tags))
                } else {
                    createPayload(name = state.name, script = script, description = state.description, category = state.category, tags = tags)
                }
            }.onSuccess {
                _uiState.update { it.copy(isSaving = false) }
                _saved.emit(Unit)
            }.onFailure { error ->
                _uiState.update { it.copy(isSaving = false, error = error.message ?: "Save failed") }
            }
        }
    }
}

data class EditorUiState(
    val isEditMode: Boolean = false,
    val name: String = "",
    val description: String = "",
    val script: String = "",
    val category: PayloadCategory = PayloadCategory.UNCATEGORIZED,
    val tagsRaw: String = "",
    val isSaving: Boolean = false,
    val error: String? = null
)
