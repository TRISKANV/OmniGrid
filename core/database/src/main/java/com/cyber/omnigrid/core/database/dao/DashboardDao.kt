package com.cyber.omnigrid.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.cyber.omnigrid.core.database.entity.ExecutionSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DashboardDao {
    // Esto alimenta el "Activity Log" en tiempo real
    @Query("""
        SELECT * FROM execution_sessions 
        WHERE workspaceId = :workspaceId 
        ORDER BY startedAt DESC LIMIT 10
    """)
    fun observeRecentActivity(workspaceId: String): Flow<List<ExecutionSessionEntity>>

    // Para el HUD del estado del sistema ("ACTIVE JOBS")
    @Query("SELECT COUNT(*) FROM execution_sessions WHERE status = 'RUNNING'")
    fun observeActiveJobsCount(): Flow<Int>
}
