package com.cyber.omnigrid.feature.automation.data.repository

import com.cyber.omnigrid.core.database.dao.PayloadDao
import com.cyber.omnigrid.core.database.entity.PayloadEntity
import com.cyber.omnigrid.feature.automation.domain.model.Payload
import com.cyber.omnigrid.feature.automation.domain.repository.PayloadRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class OfflinePayloadRepository(
    private val payloadDao: PayloadDao,
    private val currentWorkspaceId: String = "default_workspace", // TODO: Proveer dinámicamente vía DataStore/State
    private val toolId: String = "rucky_v1"                       // Hardcodeado temporalmente para el módulo Rucky
) : PayloadRepository {

    override fun observePayloads(): Flow<List<Payload>> {
        return payloadDao.observePayloadsByTool(toolId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun observeFavorites(): Flow<List<Payload>> {
        return payloadDao.observeFavoritesByTool(toolId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun savePayload(payload: Payload) {
        payloadDao.insertPayload(payload.toEntity(currentWorkspaceId, toolId))
    }

    override suspend fun toggleFavorite(payloadId: String, isFavorite: Boolean) {
        payloadDao.updateFavoriteStatus(payloadId, isFavorite)
    }

    override suspend fun deletePayload(payloadId: String) {
        payloadDao.deletePayload(payloadId)
    }
}

// Extension functions privadas para el mapeo Entidad <-> Dominio
private fun PayloadEntity.toDomain() = Payload(
    id = id,
    name = name,
    description = description,
    content = content,
    isFavorite = isFavorite
)

private fun Payload.toEntity(workspaceId: String, toolId: String) = PayloadEntity(
    id = id.ifEmpty { UUID.randomUUID().toString() },
    workspaceId = workspaceId,
    toolId = toolId,
    name = name,
    description = description,
    content = content,
    isFavorite = isFavorite
)
