package com.cyber.omnigrid.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tools")
data class ToolEntity(
    @PrimaryKey val id: String,          // Ej: "rucky_v1", "nmap_core"
    val name: String,                    // Ej: "HID Injector", "Nmap Scanner"
    val category: String,                // "AUTOMATION", "NETWORK"
    val isEnabled: Boolean = true,
    val requiresRoot: Boolean = false,
    val version: String,
    val configSchema: String? = null     // (Opcional) Un JSON que define qué campos necesita la UI para configurarlo
)
