package com.cyber.omnigrid.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.cyber.omnigrid.core.database.dao.DashboardDao
import com.cyber.omnigrid.core.database.dao.PayloadDao
import com.cyber.omnigrid.core.database.entity.ExecutionLogEntity
import com.cyber.omnigrid.core.database.entity.ExecutionSessionEntity
import com.cyber.omnigrid.core.database.entity.PayloadEntity
import com.cyber.omnigrid.core.database.entity.ToolEntity
import com.cyber.omnigrid.core.database.entity.WorkspaceEntity

@Database(
    entities = [
        WorkspaceEntity::class,
        ToolEntity::class,
        ExecutionSessionEntity::class,
        ExecutionLogEntity::class,
        PayloadEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class OmniGridDatabase : RoomDatabase() {
    abstract fun dashboardDao(): DashboardDao
    abstract fun payloadDao(): PayloadDao
}
