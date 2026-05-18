package com.cyber.omnigrid.feature.automation.presentation.manager

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cyber.omnigrid.feature.automation.domain.model.Payload
import com.cyber.omnigrid.feature.automation.domain.repository.PayloadRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class PayloadViewModel(
    private val repository: PayloadRepository
) : ViewModel() {

    // Estados locales para la búsqueda y el filtro de favoritos
    private val _searchQuery = MutableStateFlow("")
    private val _isFavoritesFilterActive = MutableStateFlow(false)

    // El StateFlow único que expondrá el estado unificado a Compose
    val uiState: StateFlow<PayloadManagerUiState> = combine(
        repository.observePayloads(),
        _searchQuery,
        _isFavoritesFilterActive
    ) { payloads, query, filterFavorites ->
        
        // 1. Aplicar reglas de negocio: Filtrado por búsqueda y favoritos
        val filteredPayloads = payloads.filter { payload ->
            val matchesQuery = payload.name.contains(query, ignoreCase = true) || 
                               payload.description.contains(query, ignoreCase = true)
            val matchesFavorite = !filterFavorites || payload.isFavorite
            
            matchesQuery && matchesFavorite
        }

        // 2. Determinar el estado de la UI basado en el resultado
        if (filteredPayloads.isEmpty() && query.isEmpty() && !filterFavorites) {
            PayloadManagerUiState.Empty
        } else {
            PayloadManagerUiState.Success(
                payloads = filteredPayloads,
                searchQuery = query,
                isFilterFavoritesActive = filterFavorites
            )
        }
    }.catch { error ->
        emit(PayloadManagerUiState.Error(error.localizedMessage ?: "Error desconocido en el sistema"))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000), // Optimiza batería/recursos al pausar la app
        initialValue = PayloadManagerUiState.Loading
    )

    /**
     * Punto de entrada único para las acciones de la UI (MVI Pattern)
     */
    fun onEvent(event: PayloadManagerUiEvent) {
        when (event) {
            is PayloadManagerUiEvent.UpdateSearchQuery -> {
                _searchQuery.value = event.query
            }
            is PayloadManagerUiEvent.ToggleFavoriteFilter -> {
                _isFavoritesFilterActive.value = event.isActive
            }
            is PayloadManagerUiEvent.TogglePayloadFavorite -> {
                viewModelScope.launch {
                    repository.toggleFavorite(event.payloadId, !event.currentStatus)
                }
            }
            is PayloadManagerUiEvent.DeletePayload -> {
                viewModelScope.launch {
                    repository.deletePayload(event.payloadId)
                }
            }
            is PayloadManagerUiEvent.CreatePayload -> {
                viewModelScope.launch {
                    val newPayload = Payload(
                        id = UUID.randomUUID().toString(),
                        name = event.name,
                        description = event.description,
                        content = event.content,
                        isFavorite = false
                    )
                    repository.savePayload(newPayload)
                }
            }
        }
    }
}
