package com.cyber.omnigrid.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "payloads")
data class PayloadEntity(
    @PrimaryKey val id: String, // UUID
    val workspaceId: String,    // Para filtrar por entorno (Ej: "Red Team Lab")
    val title: String,          // Ej: "Reverse Shell Win11"
    val category: String,       // Ej: "Recon", "Exfiltration", "Pranks"
    val scriptContent: String,  // El código DuckyScript
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val lastExecutedAt: Long? = null
)
