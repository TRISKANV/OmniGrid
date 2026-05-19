package com.cyber.omnigrid.core.os.domain

/**
 * Representa el estado actual de compatibilidad y permisos del dispositivo.
 * Desacopla la lógica de Android (SDKs) de la UI.
 */
data class SystemCapabilities(
    val isBluetoothAvailable: Boolean = false,
    val isBluetoothEnabled: Boolean = false,
    val isHidSupported: Boolean = false, // Crítico para nuestro Executor
    val missingPermissions: List<String> = emptyList(),
    val isReady: Boolean = false
)
