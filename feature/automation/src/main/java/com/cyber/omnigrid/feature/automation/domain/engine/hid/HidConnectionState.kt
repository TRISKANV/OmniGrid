package com.cyber.omnigrid.feature.automation.domain.engine.hid

/**
 * Máquina de estados finitos para cualquier adaptador de transporte.
 * Garantiza degradación elegante y feedback visual preciso.
 */
enum class HidConnectionState {
    UNSUPPORTED, // El hardware no soporta el perfil (ej. API < 28 o fabricante bloqueado)
    DISABLED,    // Bluetooth apagado o sin permisos
    IDLE,        // Servicio registrado, esperando acción
    PAIRING,     // Negociando claves con el host
    CONNECTING,  // Estableciendo el canal L2CAP
    CONNECTED,   // Enlace establecido, listo para transmitir
    ERROR        // Fallo en la capa de transporte
}
