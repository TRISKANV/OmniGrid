package com.omnigrid.payload.data.local.dao

import androidx.room.*
import com.omnigrid.payload.data.local.entity.PayloadEntity
import com.omnigrid.payload.data.local.entity.PayloadSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PayloadDao {
    @Query("SELECT * FROM payloads ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<PayloadEntity>>

    @Query("SELECT * FROM payloads WHERE category = :category ORDER BY updatedAt DESC")
    fun observeByCategory(category: String): Flow<List<PayloadEntity>>

    @Query("SELECT * FROM payloads WHERE isFavorite = 1 ORDER BY updatedAt DESC")
    fun observeFavorites(): Flow<List<PayloadEntity>>

    @Query("SELECT * FROM payloads WHERE name LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%' ORDER BY updatedAt DESC")
    fun search(query: String): Flow<List<PayloadEntity>>

    @Query("SELECT * FROM payloads WHERE id = :id")
    suspend fun getById(id: String): PayloadEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(payload: PayloadEntity)

    @Update
    suspend fun update(payload: PayloadEntity)

    @Query("DELETE FROM payloads WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("UPDATE payloads SET isFavorite = CASE WHEN isFavorite = 0 THEN 1 ELSE 0 END WHERE id = :id")
    suspend fun toggleFavorite(id: String)

    @Query("UPDATE payloads SET executionCount = executionCount + 1, lastExecutedAt = :timestamp WHERE id = :id")
    suspend fun incrementExecutionCount(id: String, timestamp: Long = System.currentTimeMillis())
}

@Dao
interface PayloadSessionDao {
    @Query("SELECT * FROM payload_sessions WHERE state NOT IN ('COMPLETED', 'FAILED', 'CANCELLED', 'TIMEOUT') ORDER BY startedAt DESC")
    fun observeActive(): Flow<List<PayloadSessionEntity>>

    @Query("SELECT * FROM payload_sessions WHERE sessionId = :sessionId")
    fun observeById(sessionId: String): Flow<PayloadSessionEntity?>

    @Query("SELECT * FROM payload_sessions WHERE payloadId = :payloadId ORDER BY startedAt DESC")
    fun observeByPayload(payloadId: String): Flow<List<PayloadSessionEntity>>

    @Query("SELECT * FROM payload_sessions ORDER BY startedAt DESC LIMIT :limit")
    fun observeHistory(limit: Int): Flow<List<PayloadSessionEntity>>

    @Query("SELECT * FROM payload_sessions WHERE sessionId = :sessionId")
    suspend fun getById(sessionId: String): PayloadSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: PayloadSessionEntity)

    @Query("UPDATE payload_sessions SET state = :state, endedAt = :endedAt WHERE sessionId = :sessionId")
    suspend fun updateState(sessionId: String, state: String, endedAt: Long?)

    @Query("UPDATE payload_sessions SET completedActions = :completed, failedActions = :failed WHERE sessionId = :sessionId")
    suspend fun updateProgress(sessionId: String, completed: Int, failed: Int)

    @Query("UPDATE payload_sessions SET logs = :logsJson WHERE sessionId = :sessionId")
    suspend fun updateLogs(sessionId: String, logsJson: String)

    @Query("UPDATE payload_sessions SET metrics = :metricsJson WHERE sessionId = :sessionId")
    suspend fun updateMetrics(sessionId: String, metricsJson: String)

    @Query("UPDATE payload_sessions SET transportState = :transport WHERE sessionId = :sessionId")
    suspend fun updateTransportState(sessionId: String, transport: String)

    @Query("DELETE FROM payload_sessions WHERE sessionId = :sessionId")
    suspend fun deleteById(sessionId: String)

    @Query("DELETE FROM payload_sessions WHERE payloadId = :payloadId")
    suspend fun deleteByPayload(payloadId: String)

    @Query("DELETE FROM payload_sessions")
    suspend fun deleteAll()
}
