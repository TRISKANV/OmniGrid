package com.cyber.omnigrid.feature.automation.domain.engine

import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * Simulador de hardware de alta fidelidad. Recrea los tiempos asíncronos de Bluetooth HID,
 * latencias de buffer y fallos de infraestructura aleatorios (si se requiere).
 */
class MockBluetoothExecutor : PayloadExecutor {
    
    override val executorName: String = "BT_HID_PERIPHERAL_SIM"

    override suspend fun connect() {
        // Simular handshake SDP y negociación de perfiles HID de Android
        delay(1500)
        if (Random.nextFloat() < 0.02f) { // 2% de probabilidad de fallo de emparejamiento para testear robustez
            throw IllegalStateException("ERR_BT_PAIRING_TIMEOUT: El dispositivo objetivo rechazó la llave de paso.")
        }
    }

    override suspend fun executeAction(action: DuckyAction) {
        when (action) {
            is DuckyAction.Delay -> {
                delay(action.timeMs)
            }
            is DuckyAction.TypeString -> {
                // Simula la velocidad de tipeo física por carácter (Key-by-Key injection)
                val typingDelay = (action.text.length * 15L).coerceIn(100L, 1200L)
                delay(typingDelay)
            }
            is DuckyAction.KeyCombo -> {
                // Simula la pulsación y liberación de modificadores (Shift, Alt, Ctrl)
                delay(120)
            }
        }
    }

    override suspend fun disconnect() {
        // Simular limpieza de buffers de transmisión de radiofrecuencia
        delay(300)
    }
}
