package com.omnigrid.payload.data.local.entity

import androidx.room.*
import com.omnigrid.payload.domain.model.*

@Entity(tableName = "payloads")
data class PayloadEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val script: String,
    val category: String,
    val tags: String,
    val isFavorite: Boolean,
    val workspaceId: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val executionCount: Int,
    val lastExecutedAt: Long?,
    val metadata: String
)

@Entity(
    tableName = "payload_sessions",
    foreignKeys = [
        ForeignKey(
            entity = PayloadEntity::class,
            parentColumns = ["id"],
            childColumns = ["payloadId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["payloadId"])]
)
data class PayloadSessionEntity(
    @PrimaryKey val sessionId: String,
    val payloadId: String,
    val payloadName: String,
    val startedAt: Long,
    val endedAt: Long?,
    val state: String,
    val totalActions: Int,
    val completedActions: Int,
    val failedActions: Int,
    val warnings: String,
    val logs: String,
    val transportState: String,
    val metrics: String
)

// ── Mappers 

fun PayloadEntity.toDomain(): Payload = Payload(
    id = id,
    name = name,
    description = description,
    script = script,
    category = PayloadCategory.valueOf(category),
    tags = tags.parseJsonStringList(),
    isFavorite = isFavorite,
    workspaceId = workspaceId,
    createdAt = createdAt,
    updatedAt = updatedAt,
    executionCount = executionCount,
    lastExecutedAt = lastExecutedAt,
    metadata = metadata.parseJsonStringMap()
)

fun Payload.toEntity(): PayloadEntity = PayloadEntity(
    id = id,
    name = name,
    description = description,
    script = script,
    category = category.name,
    tags = tags.toJsonString(),
    isFavorite = isFavorite,
    workspaceId = workspaceId,
    createdAt = createdAt,
    updatedAt = updatedAt,
    executionCount = executionCount,
    lastExecutedAt = lastExecutedAt,
    metadata = metadata.toJsonString()
)

fun PayloadSessionEntity.toDomain(): PayloadSession = PayloadSession(
    sessionId = sessionId,
    payloadId = payloadId,
    payloadName = payloadName,
    startedAt = startedAt,
    endedAt = endedAt,
    state = ExecutionState.valueOf(state),
    totalActions = totalActions,
    completedActions = completedActions,
    failedActions = failedActions,
    warnings = warnings.parseJsonStringList(),
    logs = logs.parseJsonSessionLogs(),
    transportState = TransportState.valueOf(transportState)
)

fun PayloadSession.toEntity(): PayloadSessionEntity = PayloadSessionEntity(
    sessionId = sessionId,
    payloadId = payloadId,
    payloadName = payloadName,
    startedAt = startedAt,
    endedAt = endedAt,
    state = state.name,
    totalActions = totalActions,
    completedActions = completedActions,
    failedActions = failedActions,
    warnings = warnings.toJsonString(),
    logs = logs.toJsonString(),
    transportState = transportState.name,
    metrics = "{}" // Las métricas se guardan por separado en el update final para no penalizar el render
)

// ── Helpers Nativos Nativos (Sin Gson)
private fun List<String>.toJsonString(): String = "[${joinToString(",") { "\"$it\"" }}]"
private fun Map<String, String>.toJsonString(): String = "{${entries.joinToString(",") { "\"${it.key}\":\"${it.value}\"" }}}"
private fun List<SessionLog>.toJsonString(): String = org.json.JSONArray(map { log ->
    org.json.JSONObject().apply {
        put("id", log.id)
        put("timestamp", log.timestamp)
        put("level", log.level.name)
        put("tag", log.tag)
        put("message", log.message)
        log.actionIndex?.let { put("actionIndex", it) }
    }
}).toString()

private fun String.parseJsonStringList(): List<String> = try {
    val arr = org.json.JSONArray(this)
    (0 until arr.length()).map { arr.getString(it) }
} catch (e: Exception) { emptyList() }

private fun String.parseJsonStringMap(): Map<String, String> = try {
    val obj = org.json.JSONObject(this)
    obj.keys().asSequence().associateWith { obj.getString(it) }
} catch (e: Exception) { emptyMap() }

private fun String.parseJsonSessionLogs(): List<SessionLog> = try {
    val arr = org.json.JSONArray(this)
    (0 until arr.length()).map { i ->
        val obj = arr.getJSONObject(i)
        SessionLog(
            id = obj.getString("id"),
            timestamp = obj.getLong("timestamp"),
            level = LogLevel.valueOf(obj.getString("level")),
            tag = obj.getString("tag"),
            message = obj.getString("message"),
            actionIndex = obj.optInt("actionIndex", -1).takeIf { it >= 0 }
        )
    }
} catch (e: Exception) { emptyList() }
