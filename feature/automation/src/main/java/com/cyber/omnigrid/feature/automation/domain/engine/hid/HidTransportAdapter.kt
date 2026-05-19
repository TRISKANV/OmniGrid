package com.cyber.omnigrid.feature.automation.domain.engine.hid

import kotlinx.coroutines.flow.StateFlow

/**
 * Contrato de aislamiento para la capa de transporte.
 * El motor de ejecución usa esto para orquestar la conexión y enviar ráfagas de datos
 * sin conocer la implementación de bajo nivel (Bluetooth, OTG, etc.).
 */
interface HidTransportAdapter {
    val state: StateFlow<HidConnectionState>
    
    /**
     * Comprueba capacidades del sistema, permisos e inicializa el servicio (ej. SDP).
     */
    suspend fun initialize()
    
    /**
     * Intenta conectar al último host emparejado o inicia modo discovery.
     */
    suspend fun connect()
    
    /**
     * Transmite un bloque de datos abstracto al host. 
     * (La traducción de Action a Bytes ocurre en una capa superior).
     */
    suspend fun transmitReport(reportBytes: ByteArray)
    
    /**
     * Cierra sockets y desregistra servicios.
     */
    suspend fun disconnect()
    
    /**
     * Liberación total de recursos (onCleared).
     */
    fun release()
}
