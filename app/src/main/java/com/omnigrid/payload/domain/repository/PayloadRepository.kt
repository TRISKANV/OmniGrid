package com.omnigrid.payload.domain.repository

import com.omnigrid.payload.domain.model.*
import kotlinx.coroutines.flow.Flow

interface PayloadRepository {
    // ── Payload CRUD 
    fun observeAllPayloads(): Flow<List<Payload>>
    fun observePayloadsByCategory(category: PayloadCategory): Flow<List<Payload>>
    fun observeFavorites(): Flow<List<Payload>>
    fun searchPayloads(query: String): Flow<List<Payload>>
    suspend fun getPayloadById(id: String): Payload?
    suspend fun insertPayload(payload: Payload)
    suspend fun updatePayload(payload: Payload)
    suspend fun deletePayload(id: String)
    suspend fun toggleFavorite(id: String)
    suspend fun incrementExecutionCount(id: String)

    // ── Session Management
    fun observeActiveSessions(): Flow<List<PayloadSession>>
    fun observeSessionById(sessionId: String): Flow<PayloadSession?>
    fun observeSessionsByPayload(payloadId: String): Flow<List<PayloadSession>>
    suspend fun getSessionById(sessionId: String): PayloadSession?
    suspend fun insertSession(session: PayloadSession)
    suspend fun updateSessionState(sessionId: String, state: ExecutionState, endedAt: Long? = null)
    suspend fun appendSessionLog(sessionId: String, log: SessionLog)
    suspend fun updateSessionProgress(sessionId: String, completedActions: Int, failedActions: Int)
    suspend fun updateSessionMetrics(sessionId: String, metrics: ExecutionMetrics)
    suspend fun updateTransportState(sessionId: String, transportState: TransportState)
    suspend fun deleteSession(sessionId: String)
    suspend fun deleteSessionsByPayload(payloadId: String)

    // ── History
    fun observeExecutionHistory(limit: Int = 50): Flow<List<PayloadSession>>
    suspend fun clearHistory()
}
