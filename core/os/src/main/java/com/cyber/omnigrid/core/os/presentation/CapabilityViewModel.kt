package com.cyber.omnigrid.core.os.presentation

import android.os.Build
import androidx.lifecycle.ViewModel
import com.cyber.omnigrid.core.os.data.AndroidCapabilityManager
import com.cyber.omnigrid.core.os.domain.SystemCapabilities
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Estado extendido que combina las capacidades dinámicas con información estática del hardware.
 */
data class DiagnosticsUiState(
    val capabilities: SystemCapabilities = SystemCapabilities(),
    val deviceModel: String = Build.MODEL,
    val deviceManufacturer: String = Build.MANUFACTURER,
    val apiLevel: Int = Build.VERSION.SDK_INT
)

class CapabilityViewModel(
    private val capabilityManager: AndroidCapabilityManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(DiagnosticsUiState())
    val uiState: StateFlow<DiagnosticsUiState> = _uiState.asStateFlow()

    init {
        // Observar los cambios del manager y reflejarlos en la UI agregando la info del dispositivo
        refreshCapabilities()
    }

    /**
     * Debe ser llamado desde la UI cada vez que el usuario vuelve de los ajustes 
     * o interactúa con los diálogos de permisos.
     */
    fun refreshCapabilities() {
        capabilityManager.refreshCapabilities()
        _uiState.update { 
            it.copy(capabilities = capabilityManager.capabilities.value) 
        }
    }
}
