package com.cyber.omnigrid.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workspaces")
data class WorkspaceEntity(
    @PrimaryKey val id: String,
    val name: String,                    // Ej: "Red Team Lab"
    val colorAccent: Long,               // Para pintar la UI
    val isDefault: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
