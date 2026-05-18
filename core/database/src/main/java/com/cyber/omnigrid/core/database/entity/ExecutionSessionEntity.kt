package com.cyber.omnigrid.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "execution_sessions")
data class ExecutionSessionEntity(
    @PrimaryKey val id: String,             // UUID de la sesión
    val workspaceId: String,                // FK lógica a Workspace
    val toolId: String,                     // FK lógica a Tool (Ej: rucky_v1)
    val target: String?,                    // Ej: "192.168.1.1" o "Windows 11 Target"
    val status: SessionStatus,              // Enum: RUNNING, SUCCESS, ERROR, ABORTED
    val startedAt: Long = System.currentTimeMillis(),
    val endedAt: Long? = null,
    
    // Acá entra tu concepto de ParserOutputModel:
    // Guardamos el resultado final ya parseado e higienizado (idealmente un JSON genérico)
    // para que la UI lo renderice directo en una CyberCard sin saber qué herramienta lo generó.
    val parsedResultData: String? = null    
)

enum class SessionStatus { RUNNING, SUCCESS, ERROR, ABORTED }
