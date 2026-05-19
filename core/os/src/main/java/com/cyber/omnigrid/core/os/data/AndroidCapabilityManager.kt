package com.cyber.omnigrid.core.os.data

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.cyber.omnigrid.core.os.domain.SystemCapabilities
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Gestor defensivo contra la fragmentación de Android.
 * Evalúa las capacidades del OEM y el estado de los permisos en tiempo real.
 */
class AndroidCapabilityManager(private val context: Context) {

    private val TAG = "AndroidCapabilityManager"

    private val _capabilities = MutableStateFlow(SystemCapabilities())
    val capabilities: StateFlow<SystemCapabilities> = _capabilities.asStateFlow()

    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        bluetoothManager?.adapter
    }

    /**
     * Debe llamarse cada vez que la app entra en onResume o después de pedir un permiso.
     */
    fun refreshCapabilities() {
        val btAvailable = bluetoothAdapter != null
        val btEnabled = bluetoothAdapter?.isEnabled == true
        val missingPerms = getMissingPermissions()
        
        // Verificación de hardware HID (Defensivo OEM). 
        // Si el dispositivo no tiene Bluetooth, definitivamente no soporta HID.
        val hidSupported = checkHidCapability(btAvailable)

        val ready = btAvailable && btEnabled && hidSupported && missingPerms.isEmpty()

        _capabilities.update {
            it.copy(
                isBluetoothAvailable = btAvailable,
                isBluetoothEnabled = btEnabled,
                isHidSupported = hidSupported,
                missingPermissions = missingPerms,
                isReady = ready
            )
        }
        
        Log.d(TAG, "Capacidades actualizadas: Ready=$ready, MissingPerms=${missingPerms.size}, HID_Support=$hidSupported")
    }

    private fun checkHidCapability(btAvailable: Boolean): Boolean {
        if (!btAvailable) return false
        
        // Muchos fabricantes (ej. algunas ROMs de Xiaomi/Samsung baratas) deshabilitan el HID Profile 
        // a nivel de Kernel. Esto requerirá una validación más profunda cuando implementemos 
        // la conexión BluetoothHidDevice real, pero por ahora asumimos soporte si BT está presente.
        return true 
    }

    private fun getMissingPermissions(): List<String> {
        val requiredPermissions = mutableListOf<String>()

        // 1. Permisos de Bluetooth según versión de Android
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // Android 12+
            requiredPermissions.add(Manifest.permission.BLUETOOTH_CONNECT)
            requiredPermissions.add(Manifest.permission.BLUETOOTH_SCAN)
        } else { // Android 11 o inferior
            requiredPermissions.add(Manifest.permission.BLUETOOTH)
            requiredPermissions.add(Manifest.permission.BLUETOOTH_ADMIN)
            // Localización requerida en versiones antiguas para escanear BT
            requiredPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION) 
        }

        // 2. Permisos de Foreground Service
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) { // Android 9+
            requiredPermissions.add(Manifest.permission.FOREGROUND_SERVICE)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) { // Android 14+
            requiredPermissions.add(Manifest.permission.FOREGROUND_SERVICE_CONNECTED_DEVICE)
        }

        // Filtrar solo los que NO están concedidos
        return requiredPermissions.filter { permission ->
            ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED
        }
    }
}
