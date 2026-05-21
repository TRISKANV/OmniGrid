package com.tuapp.calculadora.ui.system.sdk

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

enum class PluginCategory {
    SYSTEM, 
    TELEMETRY, 
    DIAGNOSTICS, 
    UI_MOD, 
    SECURITY // <- Agregado para el SecureVault
}

enum class SystemCapability {
    HARDWARE_TELEMETRY,
    SYSTEM_DIAGNOSTICS,
    NETWORK_MONITOR,
    SECURE_STORAGE,      // <- Agregado
    CRYPTO_ACCELERATION  // <- Agregado
}

data class PluginManifest(
    val pluginId: String,
    val displayName: String,
    val version: String,
    val description: String,
    val category: PluginCategory,
    val providedCapabilities: Set<SystemCapability>,
    val consumedCapabilities: Set<SystemCapability>,
    val requiredPermissions: List<String>,
    val visualPriority: Int,
    val supportsHeadlessExecution: Boolean,
    val transportCompatibility: List<String>
)

interface PluginWidgetProvider {
    @Composable
    fun Render(modifier: Modifier)
    fun onWidgetVisible()
    fun onWidgetHidden()
}

interface OmniPlugin {
    val manifest: PluginManifest
    val widgetProvider: PluginWidgetProvider?
    
    fun onInstall()
    fun onBoot()
    fun onSuspend()
    fun onDestroy()
    fun executeAction(actionId: String, payload: Map<String, Any>): Result<Unit>
    fun getHealthStatus(): String
}
