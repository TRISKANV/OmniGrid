package com.cyber.omnigrid.feature.automation.data.hid

import android.util.Log
import com.cyber.omnigrid.feature.automation.domain.engine.hid.HidConnectionState
import com.cyber.omnigrid.feature.automation.domain.engine.hid.HidTransportAdapter
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Adaptador de prueba para simular el ciclo de vida del Bluetooth sin hardware.
 * Vital para los Unit Tests y el desarrollo del UI/UX.
 */
class MockHidAdapter : HidTransportAdapter {

    private val TAG = "MockHidAdapter"
    private val _state = MutableStateFlow(HidConnectionState.IDLE)
    override val state: StateFlow<HidConnectionState> = _state.asStateFlow()

    override suspend fun initialize() {
        Log.i(TAG, "[MOCK] Inicializando capacidades del sistema...")
        delay(500)
        _state.value = HidConnectionState.IDLE
    }

    override suspend fun connect() {
        Log.i(TAG, "[MOCK] Iniciando secuencia de emparejamiento simulada...")
        _state.value = HidConnectionState.CONNECTING
        delay(1200) // Simular latencia de handshake SDP
        _state.value = HidConnectionState.CONNECTED
        Log.i(TAG, "[MOCK] Conexión establecida.")
    }

    override suspend fun transmitReport(reportBytes: ByteArray) {
        if (_state.value != HidConnectionState.CONNECTED) return
        Log.d(TAG, "[MOCK] Transmitiendo descriptor de ${reportBytes.size} bytes...")
        delay(15) // Simular latencia de buffer HID
    }

    override suspend fun disconnect() {
        Log.i(TAG, "[MOCK] Desconectando...")
        delay(300)
        _state.value = HidConnectionState.IDLE
    }

    override fun release() {
        Log.i(TAG, "[MOCK] Liberando recursos.")
        _state.value = HidConnectionState.DISABLED
    }
}
