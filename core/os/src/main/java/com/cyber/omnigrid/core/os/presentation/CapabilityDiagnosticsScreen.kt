package com.cyber.omnigrid.core.os.presentation

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cyber.omnigrid.core.designsystem.components.CyberCard
import com.cyber.omnigrid.core.designsystem.theme.*

@Composable
fun CapabilityDiagnosticsScreen(
    viewModel: CapabilityViewModel,
    onContinue: () -> Unit, // Navegar al Dashboard si todo está listo
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val caps = state.capabilities

    // 1. Launcher para pedir permisos de Android de forma reactiva
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        // Actualizamos el estado independientemente de lo que respondió el usuario
        viewModel.refreshCapabilities()
    }

    // 2. Launcher para pedir activación de Bluetooth nativa
    val bluetoothLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { _ ->
        viewModel.refreshCapabilities()
    }

    // Lifecycle Hook: refrescar cuando la pantalla entra en composición
    LaunchedEffect(Unit) {
        viewModel.refreshCapabilities()
    }

    Scaffold(
        containerColor = TrueBlack
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- HEADER ---
            Column {
                Text(text = "// SYSTEM_DIAGNOSTICS", color = CyberAccent, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                Text(text = "ENVIRONMENT CHECK", color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                Text(text = "Validando infraestructura de inyección y permisos del sistema operativo.", color = TextSecondary, fontSize = 14.sp)
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                // --- TARGET DEVICE INFO ---
                item {
                    CyberCard(modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(text = "DEVICE FINGERPRINT", color = TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = "OEM:", color = TextPrimary, fontSize = 12.sp)
                                Text(text = state.deviceManufacturer.uppercase(), color = CyberAccent, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = "MODEL:", color = TextPrimary, fontSize = 12.sp)
                                Text(text = state.deviceModel.uppercase(), color = CyberAccent, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = "API LEVEL:", color = TextPrimary, fontSize = 12.sp)
                                Text(text = "${state.apiLevel}", color = CyberAccent, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                }

                // --- HARDWARE CAPABILITIES ---
                item {
                    Text(text = "HARDWARE LAYER", color = TextSecondary, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
                }
                
                item {
                    StatusRow(
                        title = "Bluetooth Adapter",
                        description = if (caps.isBluetoothAvailable) "Hardware detectado" else "No soportado en este dispositivo",
                        isOk = caps.isBluetoothAvailable
                    )
                }

                item {
                    StatusRow(
                        title = "Bluetooth Power",
                        description = if (caps.isBluetoothEnabled) "Adaptador encendido" else "Adaptador apagado",
                        isOk = caps.isBluetoothEnabled,
                        actionLabel = if (!caps.isBluetoothEnabled && caps.isBluetoothAvailable) "TURN ON" else null,
                        onAction = { 
                            bluetoothLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)) 
                        }
                    )
                }

                item {
                    StatusRow(
                        title = "HID Protocol Support",
                        description = if (caps.isHidSupported) "Perfil Periférico soportado" else "Restringido por OEM/Versión",
                        isOk = caps.isHidSupported
                    )
                }

                // --- PERMISSION LAYER ---
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "OS PERMISSIONS", color = TextSecondary, fontSize = 12.sp)
                        if (caps.missingPermissions.isNotEmpty()) {
                            Button(
                                onClick = { permissionLauncher.launch(caps.missingPermissions.toTypedArray()) },
                                colors = ButtonDefaults.buttonColors(containerColor = CyberAccent),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("GRANT ALL", color = TrueBlack, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                if (caps.missingPermissions.isEmpty()) {
                    item {
                        StatusRow(title = "Access Rights", description = "Todos los permisos concedidos", isOk = true)
                    }
                } else {
                    items(caps.missingPermissions) { permission ->
                        val (friendlyName, desc) = mapPermissionToFriendlyName(permission)
                        StatusRow(title = friendlyName, description = desc, isOk = false)
                    }
                }
            }

            // --- BOTTOM ACTION ---
            val isReady = caps.isReady
            Button(
                onClick = onContinue,
                enabled = isReady,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isReady) Color(0xFF00FF66) else DarkGrayCard,
                    disabledContainerColor = DarkGrayCard
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .border(1.dp, if (isReady) Color(0xFF00FF66) else BorderGray, RoundedCornerShape(8.dp))
            ) {
                Text(
                    text = if (isReady) "SYSTEM READY // CONTINUE" else "ENVIRONMENT NOT READY",
                    color = if (isReady) TrueBlack else TextSecondary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
private fun StatusRow(
    title: String,
    description: String,
    isOk: Boolean,
    actionLabel: String? = null,
    onAction: () -> Unit = {}
) {
    val statusColor = if (isOk) Color(0xFF00FF66) else Color(0xFFFF3333)
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF030303), RoundedCornerShape(8.dp))
            .border(1.dp, BorderGray, RoundedCornerShape(8.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(8.dp).background(statusColor, RoundedCornerShape(4.dp)))
            Column {
                Text(text = title, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(text = description, color = TextSecondary, fontSize = 11.sp)
            }
        }

        if (actionLabel != null) {
            TextButton(onClick = onAction) {
                Text(text = actionLabel, color = CyberAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// Función auxiliar para traducir los strings crudos de Android a UI amigable
private fun mapPermissionToFriendlyName(permission: String): Pair<String, String> {
    return when {
        permission.contains("BLUETOOTH_CONNECT") -> "Bluetooth Connect" to "Requerido para emparejar con el host."
        permission.contains("BLUETOOTH_SCAN") -> "Bluetooth Scan" to "Requerido para descubrir el host."
        permission.contains("BLUETOOTH") -> "Bluetooth Legacy" to "Requerido para operar radiofrecuencia."
        permission.contains("LOCATION") -> "Location (Legacy Scan)" to "Requerido por Android < 12 para escanear."
        permission.contains("FOREGROUND_SERVICE") -> "Foreground Execution" to "Previene que Android mate la inyección."
        else -> permission.substringAfterLast(".") to "Requisito del sistema."
    }
}
