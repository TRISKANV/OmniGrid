package com.cyber.omnigrid.feature.automation.data.hid

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHidDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.os.Build
import android.util.Log
import com.cyber.omnigrid.feature.automation.domain.engine.hid.HidConnectionState
import com.cyber.omnigrid.feature.automation.domain.engine.hid.HidTransportAdapter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.Executors

/**
 * Gestor del ciclo de vida de Bluetooth HID en Android.
 * Controla permisos, chequeo de capacidades OEM y registro de perfiles.
 * NO contiene lógica de inyección de payloads.
 */
class AndroidBluetoothHidManager(
    private val context: Context
) : HidTransportAdapter {

    private val TAG = "AndroidHidManager"
    
    private val _state = MutableStateFlow(HidConnectionState.IDLE)
    override val state: StateFlow<HidConnectionState> = _state.asStateFlow()

    private val bluetoothManager: BluetoothManager? = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter
    private var hidDeviceService: BluetoothHidDevice? = null
    
    // Ejecutor para los callbacks del sistema Bluetooth
    private val callbackExecutor = Executors.newSingleThreadExecutor()

    // Callback del estado del servicio HID y de la conexión con el Host
    private val hidDeviceCallback = object : BluetoothHidDevice.Callback() {
        override fun onAppStatusChanged(pluggedDevice: BluetoothDevice?, registered: Boolean) {
            super.onAppStatusChanged(pluggedDevice, registered)
            Log.d(TAG, "SDP App Status Changed. Registered: $registered")
            if (registered) {
                _state.value = HidConnectionState.IDLE
            } else {
                _state.value = HidConnectionState.DISABLED
            }
        }

        override fun onConnectionStateChanged(device: BluetoothDevice?, state: Int) {
            super.onConnectionStateChanged(device, state)
            when (state) {
                BluetoothProfile.STATE_CONNECTING -> _state.value = HidConnectionState.CONNECTING
                BluetoothProfile.STATE_CONNECTED -> _state.value = HidConnectionState.CONNECTED
                BluetoothProfile.STATE_DISCONNECTING -> _state.value = HidConnectionState.IDLE
                BluetoothProfile.STATE_DISCONNECTED -> _state.value = HidConnectionState.IDLE
            }
            Log.d(TAG, "Connection State Changed: ${this@AndroidBluetoothHidManager._state.value}")
        }
    }

    // Listener del perfil de servicio Bluetooth
    private val profileServiceListener = object : BluetoothProfile.ServiceListener {
        override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
            if (profile == BluetoothProfile.HID_DEVICE) {
                Log.i(TAG, "HID Profile Proxy Connected")
                hidDeviceService = proxy as BluetoothHidDevice
                registerApp()
            }
        }

        override fun onServiceDisconnected(profile: Int) {
            if (profile == BluetoothProfile.HID_DEVICE) {
                Log.w(TAG, "HID Profile Proxy Disconnected")
                hidDeviceService = null
                _state.value = HidConnectionState.ERROR
            }
        }
    }

    @SuppressLint("MissingPermission") // La capa UI/ViewModel debe garantizar los permisos antes de instanciar esto
    override suspend fun initialize() {
        Log.i(TAG, "Inicializando comprobaciones de hardware y API...")
        
        // 1. Capability Checks (Degradación segura)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            Log.e(TAG, "HID Device no soportado. Requiere Android 9+ (API 28).")
            _state.value = HidConnectionState.UNSUPPORTED
            return
        }

        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            Log.w(TAG, "Bluetooth apagado o sin soporte en el hardware.")
            _state.value = HidConnectionState.DISABLED
            return
        }

        // 2. Vincular el perfil HID_DEVICE
        _state.value = HidConnectionState.IDLE
        val profileBound = bluetoothAdapter.getProfileProxy(context, profileServiceListener, BluetoothProfile.HID_DEVICE)
        
        if (!profileBound) {
            Log.e(TAG, "Fallo al vincular el perfil HID_DEVICE. Posible restricción del OEM.")
            _state.value = HidConnectionState.UNSUPPORTED
        }
    }

    @SuppressLint("MissingPermission")
    private fun registerApp() {
        // En una implementación real, aquí se define el sdpRecord y qosSettings (Estructura teórica del HID Descriptor)
        // Para este framework, solo registramos el ciclo de vida.
        Log.d(TAG, "Solicitando registro de la aplicación en el servicio Bluetooth...")
        hidDeviceService?.registerApp(
            null, // sdp
            null, // inQos
            null, // outQos
            callbackExecutor,
            hidDeviceCallback
        )
    }

    override suspend fun connect() {
        if (_state.value == HidConnectionState.UNSUPPORTED || _state.value == HidConnectionState.DISABLED) {
            throw IllegalStateException("Estado inválido para conexión: ${_state.value}")
        }
        _state.value = HidConnectionState.CONNECTING
        // Lógica teórica de host discovery o conexión a bonded devices...
    }

    override suspend fun transmitReport(reportBytes: ByteArray) {
        if (_state.value != HidConnectionState.CONNECTED) {
            Log.e(TAG, "Intento de transmisión sin conexión activa.")
            return
        }
        // Infraestructura preparada: hidDeviceService?.sendReport(...)
    }

    @SuppressLint("MissingPermission")
    override suspend fun disconnect() {
        Log.d(TAG, "Solicitando desconexión del host...")
        // Desconectar dispositivos conectados
        val connectedDevices = hidDeviceService?.connectedDevices ?: emptyList()
        connectedDevices.forEach { device ->
            hidDeviceService?.disconnect(device)
        }
        _state.value = HidConnectionState.IDLE
    }

    @SuppressLint("MissingPermission")
    override fun release() {
        Log.i(TAG, "Liberando recursos de red y callbacks...")
        hidDeviceService?.unregisterApp()
        bluetoothAdapter?.closeProfileProxy(BluetoothProfile.HID_DEVICE, hidDeviceService)
        callbackExecutor.shutdown()
    }
}
