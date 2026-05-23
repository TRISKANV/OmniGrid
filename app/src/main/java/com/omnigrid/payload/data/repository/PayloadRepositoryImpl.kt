package com.omnigrid.payload.data.repository

import com.omnigrid.payload.data.local.dao.PayloadDao
import com.omnigrid.payload.data.local.dao.PayloadSessionDao
import com.omnigrid.payload.data.local.entity.*
import com.omnigrid.payload.domain.model.*
import com.omnigrid.payload.domain.repository.PayloadRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PayloadRepositoryImpl(
    private val payloadDao: PayloadDao,
    private val sessionDao: PayloadSessionDao
) : PayloadRepository {

    override fun observeAllPayloads(): Flow<List<Payload>> =
        payloadDao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observePayloadsByCategory(category: PayloadCategory): Flow<List<Payload>> =
        payloadDao.observeByCategory(category.name).map { it.map { e -> e.toDomain() } }

    override fun observeFavorites(): Flow<List<Payload>> =
        payloadDao.observeFavorites().map { it.map { e -> e.toDomain() } }

    override fun searchPayloads(query: String): Flow<List<Payload>> =
        payloadDao.search(query).map { it.map { e -> e.toDomain() } }

    override suspend fun getPayloadById(id: String): Payload? =
        payloadDao.getById(id)?.toDomain()

    override suspend fun insertPayload(payload: Payload) =
        payloadDao.insert(payload.toEntity())

    override suspend fun updatePayload(payload: Payload) =
        payloadDao.update(payload.toEntity())

    override suspend fun deletePayload(id: String) =
        payloadDao.deleteById(id)

    override suspend fun toggleFavorite(id: String) =
        payloadDao.toggleFavorite(id)

    override suspend fun incrementExecutionCount(id: String) =
        payloadDao.incrementExecutionCount(id)

    override fun observeActiveSessions(): Flow<List<PayloadSession>> =
        sessionDao.observeActive().map { it.map { e -> e.toDomain() } }

    override fun observeSessionById(sessionId: String): Flow<PayloadSession?> =
        sessionDao.observeById(sessionId).map { it?.toDomain() }

    override fun observeSessionsByPayload(payloadId: String): Flow<List<PayloadSession>> =
        sessionDao.observeByPayload(payloadId).map { it.map { e -> e.toDomain() } }

    override suspend fun getSessionById(sessionId: String): PayloadSession? =
        sessionDao.getById(sessionId)?.toDomain()

    override suspend fun insertSession(session: PayloadSession) =
        sessionDao.insert(session.toEntity())

    override suspend fun updateSessionState(sessionId: String, state: ExecutionState, endedAt: Long?) = 
        sessionDao.updateState(sessionId, state.name, endedAt)

    override suspend fun appendSessionLog(sessionId: String, log: SessionLog) {
        val session = sessionDao.getById(sessionId) ?: return
        val current = session.toDomain()
        val updatedLogs = current.logs + log
        val updated = current.copy(logs = updatedLogs)
        sessionDao.updateLogs(sessionId, updated.toEntity().logs)
    }

    override suspend fun updateSessionProgress(sessionId: String, completedActions: Int, failedActions: Int) = 
        sessionDao.updateProgress(sessionId, completedActions, failedActions)

    override suspend fun updateSessionMetrics(sessionId: String, metrics: ExecutionMetrics) {
        val json = org.json.JSONObject().apply {
            put("queueWaitMs", metrics.queueWaitMs)
            put("initTimeMs", metrics.initTimeMs)
            put("totalExecutionMs", metrics.totalExecutionMs)
            put("avgActionLatencyMs", metrics.avgActionLatencyMs)
            put("peakActionLatencyMs", metrics.peakActionLatencyMs)
            put("retryCount", metrics.retryCount)
            put("bytesSent", metrics.bytesSent)
        }.toString()
        sessionDao.updateMetrics(sessionId, json)
    }

    override suspend fun updateTransportState(sessionId: String, transportState: TransportState) = 
        sessionDao.updateTransportState(sessionId, transportState.name)

    override suspend fun deleteSession(sessionId: String) =
        sessionDao.deleteById(sessionId)

    override suspend fun deleteSessionsByPayload(payloadId: String) =
        sessionDao.deleteByPayload(payloadId)

    override fun observeExecutionHistory(limit: Int): Flow<List<PayloadSession>> =
        sessionDao.observeHistory(limit).map { it.map { e -> e.toDomain() } }

    override suspend fun clearHistory() =
        sessionDao.deleteAll()
}
