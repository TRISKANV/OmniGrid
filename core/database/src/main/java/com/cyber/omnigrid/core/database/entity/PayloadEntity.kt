package com.cyber.omnigrid.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "payloads")
data class PayloadEntity(
    @PrimaryKey val id: String,
    val workspaceId: String,
    val toolId: String,              // Ej: "rucky_v1"
    val name: String,
    val description: String,
    val content: String,             // El script Ducky/Bash en texto plano (luego pasará por AES/GCM)
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
