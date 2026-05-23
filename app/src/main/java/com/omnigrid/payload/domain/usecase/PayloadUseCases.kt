package com.omnigrid.payload.domain.usecase

import com.omnigrid.payload.domain.model.*
import com.omnigrid.payload.domain.repository.PayloadRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class CreatePayloadUseCase(private val repository: PayloadRepository) {
    suspend operator fun invoke(
        name: String,
        script: String,
        description: String = "",
        category: PayloadCategory = PayloadCategory.UNCATEGORIZED,
        tags: List<String> = emptyList(),
        workspaceId: String? = null
    ): Payload {
        require(name.isNotBlank()) { "Payload name cannot be blank" }
        require(script.isNotBlank()) { "Payload script cannot be blank" }

        val payload = Payload(
            name = name.trim(),
            description = description.trim(),
            script = script,
            category = category,
            tags = tags.map { it.trim().lowercase() }.distinct(),
            workspaceId = workspaceId
        )
        repository.insertPayload(payload)
        return payload
    }
}

class UpdatePayloadUseCase(private val repository: PayloadRepository) {
    suspend operator fun invoke(payload: Payload) {
        require(payload.name.isNotBlank()) { "Payload name cannot be blank" }
        val updated = payload.copy(updatedAt = System.currentTimeMillis())
        repository.updatePayload(updated)
    }
}

class DeletePayloadUseCase(private val repository: PayloadRepository) {
    suspend operator fun invoke(payloadId: String) {
        repository.deleteSessionsByPayload(payloadId)
        repository.deletePayload(payloadId)
    }
}

class GetPayloadsUseCase(private val repository: PayloadRepository) {
    fun all(): Flow<List<Payload>> = repository.observeAllPayloads()
    fun byCategory(category: PayloadCategory): Flow<List<Payload>> = repository.observePayloadsByCategory(category)
    fun favorites(): Flow<List<Payload>> = repository.observeFavorites()
    fun search(query: String): Flow<List<Payload>> =
        if (query.isBlank()) repository.observeAllPayloads() else repository.searchPayloads(query)
    fun byTag(tag: String): Flow<List<Payload>> =
        repository.observeAllPayloads().map { payloads -> payloads.filter { it.tags.contains(tag.lowercase()) } }
}

class ExecutePayloadUseCase(private val repository: PayloadRepository) {
    suspend operator fun invoke(payloadId: String): PayloadSession {
        val payload = requireNotNull(repository.getPayloadById(payloadId)) { "Payload $payloadId not found" }
        val session = PayloadSession(
            payloadId = payloadId,
            payloadName = payload.name,
            state = ExecutionState.QUEUED
        )
        repository.insertSession(session)
        repository.incrementExecutionCount(payloadId)
        return session
    }
}

class GetSessionLogsUseCase(private val repository: PayloadRepository) {
    fun observe(sessionId: String): Flow<PayloadSession?> = repository.observeSessionById(sessionId)
    fun observeForPayload(payloadId: String): Flow<List<PayloadSession>> = repository.observeSessionsByPayload(payloadId)
    fun observeHistory(): Flow<List<PayloadSession>> = repository.observeExecutionHistory()
}

class ManageSessionUseCase(private val repository: PayloadRepository) {
    suspend fun cancel(sessionId: String) {
        repository.updateSessionState(sessionId, ExecutionState.CANCELLED, System.currentTimeMillis())
    }
    suspend fun pause(sessionId: String) {
        repository.updateSessionState(sessionId, ExecutionState.PAUSED)
    }
    suspend fun resume(sessionId: String) {
        repository.updateSessionState(sessionId, ExecutionState.RUNNING)
    }
    fun observeActive(): Flow<List<PayloadSession>> = repository.observeActiveSessions()

    fun observeDashboardStats(): Flow<DashboardStats> =
        combine(repository.observeActiveSessions(), repository.observeExecutionHistory(limit = 100)) { active, history ->
            DashboardStats(
                activeSessions = active.size,
                totalExecutions = history.size,
                successRate = history.takeIf { it.isNotEmpty() }?.let { sessions ->
                    sessions.count { it.state == ExecutionState.COMPLETED }.toFloat() / sessions.size.toFloat()
                } ?: 0f,
                failureCount = history.count { it.state == ExecutionState.FAILED }
            )
        }
}

data class DashboardStats(
    val activeSessions: Int,
    val totalExecutions: Int,
    val successRate: Float,
    val failureCount: Int
)
