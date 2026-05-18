package com.cyber.omnigrid.feature.automation.domain.repository

import com.cyber.omnigrid.feature.automation.domain.model.Payload
import kotlinx.coroutines.flow.Flow

interface PayloadRepository {
    fun observePayloads(): Flow<List<Payload>>
    fun observeFavorites(): Flow<List<Payload>>
    suspend fun savePayload(payload: Payload)
    suspend fun toggleFavorite(payloadId: String, isFavorite: Boolean)
    suspend fun deletePayload(payloadId: String)
}
