package com.cyber.omnigrid.feature.automation.presentation.manager

import com.cyber.omnigrid.feature.automation.domain.model.Payload

/**
 * Estados del contenedor principal del Gestor de Payloads.
 */
sealed interface PayloadManagerUiState {
    object Loading : PayloadManagerUiState
    object Empty : PayloadManagerUiState
    
    data class Success(
        val payloads: List<Payload> = emptyList(),
        val searchQuery: String = "",
        val isFilterFavoritesActive: Boolean = false
    ) : PayloadManagerUiState
    
    data class Error(val message: String) : PayloadManagerUiState
}

/**
 * Intenciones o Eventos que la UI puede enviar al ViewModel.
 */
sealed interface PayloadManagerUiEvent {
    data class UpdateSearchQuery(val query: String) : PayloadManagerUiEvent
    data class ToggleFavoriteFilter(val isActive: Boolean) : PayloadManagerUiEvent
    data class TogglePayloadFavorite(val payloadId: String, val currentStatus: Boolean) : PayloadManagerUiEvent
    data class DeletePayload(val payloadId: String) : PayloadManagerUiEvent
    data class CreatePayload(val name: String, val description: String, val content: String) : PayloadManagerUiEvent
}
