package com.cyber.omnigrid.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.cyber.omnigrid.core.database.entity.PayloadEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PayloadDao {
    @Query("SELECT * FROM payloads WHERE toolId = :toolId ORDER BY createdAt DESC")
    fun observePayloadsByTool(toolId: String): Flow<List<PayloadEntity>>

    @Query("SELECT * FROM payloads WHERE isFavorite = 1 AND toolId = :toolId ORDER BY createdAt DESC")
    fun observeFavoritesByTool(toolId: String): Flow<List<PayloadEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayload(payload: PayloadEntity)

    @Query("UPDATE payloads SET isFavorite = :isFavorite WHERE id = :payloadId")
    suspend fun updateFavoriteStatus(payloadId: String, isFavorite: Boolean)

    @Query("DELETE FROM payloads WHERE id = :payloadId")
    suspend fun deletePayload(payloadId: String)
}
