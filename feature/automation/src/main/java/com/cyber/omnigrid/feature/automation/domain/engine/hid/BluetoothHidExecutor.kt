package com.cyber.omnigrid.feature.automation.domain.engine.hid

import android.util.Log
import com.cyber.omnigrid.feature.automation.domain.engine.DuckyAction
import com.cyber.omnigrid.feature.automation.domain.engine.PayloadExecutor
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout
import kotlin.system.measureTimeMillis

/**
 * Adaptador universal para hardware HID.
 * Mapea los estados reactivos del transporte a comportamiento síncrono/excepciones 
 * que el OmniExecutionEngine puede entender y renderizar en la UI.
 */
class BluetoothHidExecutor(
    private val transportAdapter: HidTransportAdapter
) : PayloadExecutor {

    override val executorName: String = "BT_HID_CORE_V1"
    private val TAG = "BluetoothHidExecutor"

    override suspend fun connect() {
        Log.i(TAG, "Iniciando secuencia de conexión HID...")
        
        // 1. Inicialización y chequeo de capacidades (Fallback seguro)
        val initTime = measureTimeMillis {
            transportAdapter.initialize()
        }
        Log.d(TAG, "Inicialización de hardware completada en ${initTime}ms")

        val state = transportAdapter.state.value
        if (state == HidConnectionState.UNSUPPORTED || state == HidConnectionState.DISABLED) {
            // Esta excepción es atrapada por OmniExecutionEngine y mostrada en la UI como ERROR rojo.
            throw IllegalStateException("Abortado: Hardware incompatible o permisos Bluetooth insuficientes ($state).")
        }

        // 2. Proceso de emparejamiento y conexión con Timeout estricto (10 segundos)
        try {
            val connectTime = measureTimeMillis {
                transportAdapter.connect()
                
                // Arquitectura StateFlow-driven: Esperamos reactivamente hasta que el estado cambie a éxito o error crítico
                withTimeout(10_000L) {
                    transportAdapter.state.first { 
                        it == HidConnectionState.CONNECTED || it == HidConnectionState.ERROR 
                    }
                }
            }
            
            val finalState = transportAdapter.state.value
            if (finalState != HidConnectionState.CONNECTED) {
                throw IllegalStateException("Fallo en la negociación SDP. El Host rechazó la conexión.")
            }
            
            Log.i(TAG, "Handshake Bluetooth establecido exitosamente. Latencia: ${connectTime}ms")

        } catch (e: TimeoutCancellationException) {
            // Cancelación segura: Si el tiempo expira, limpiamos el transporte antes de lanzar el error a la UI
            Log.e(TAG, "Timeout alcanzado. Abortando transporte.")
            transportAdapter.disconnect()
            throw IllegalStateException("Timeout (10s): El dispositivo destino no respondió al emparejamiento.")
        }
    }

    override suspend fun executeAction(action: DuckyAction) {
        val actionTime = measureTimeMillis {
            when (action) {
                is DuckyAction.Delay -> {
                    delay(action.timeMs)
                }
                is DuckyAction.TypeString -> {
                    // Aquí en el futuro se hace la traducción: Character -> HID KeyCode
                    val simulatedReport = ByteArray(8) 
                    transportAdapter.transmitReport(simulatedReport)
                }
                is DuckyAction.KeyCombo -> {
                    // Aquí se procesan los modificadores (GUI, CTRL)
                    val simulatedReport = ByteArray(8)
                    transportAdapter.transmitReport(simulatedReport)
                }
            }
        }
        // Logs de diagnóstico internos (No saturan la UI, van al Logcat para debugging técnico)
        Log.v(TAG, "Ejecutada acción [${action.javaClass.simpleName}] en ${actionTime}ms")
    }

    override suspend fun disconnect() {
        Log.i(TAG, "Iniciando apagado seguro del transporte HID...")
        val disconnectTime = measureTimeMillis {
            transportAdapter.disconnect()
            transportAdapter.release()
        }
        Log.d(TAG, "Sockets cerrados y recursos liberados en ${disconnectTime}ms")
    }
}
