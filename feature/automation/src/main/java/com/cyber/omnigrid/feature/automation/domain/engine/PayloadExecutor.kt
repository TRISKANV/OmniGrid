package com.cyber.omnigrid.feature.automation.domain.engine

/**
 * Contrato estricto para cualquier implementación de hardware.
 */
interface PayloadExecutor {
    val executorName: String

    /**
     * Intenta establecer conexión con el target (Ej: Handshake Bluetooth).
     * Lanza excepción si falla.
     */
    suspend fun connect()

    /**
     * Ejecuta una acción atómica.
     */
    suspend fun executeAction(action: DuckyAction)

    /**
     * Liberación segura de recursos. Obligatorio llamarlo al finalizar o fallar.
     */
    suspend fun disconnect()
}
