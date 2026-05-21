package com.tuapp.calculadora.ui.system.sdk

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

// ==========================================================================
// 1. CAPABILITY-DRIVEN PLATFORM
// Fusión de capacidades tácticas y de seguridad
// ==========================================================================
enum class SystemCapability {
    HID_INJECTION,
    NETWORK_OBSERVABILITY,
    HARDWARE_TELEMETRY,
    SECURE_ENCRYPTION,
    RUNTIME_EXECUTION,
    TRANSPORT_MESH,
    SIGNAL_INTELLIGENCE,
    SYSTEM_DIAGNOSTICS,
    NETWORK_MONITOR,
    SECURE_STORAGE,
    CRYPTO_ACCELERATION
}

enum class PluginCategory {
    SYSTEM_CORE, 
    TELEMETRY, 
    OFFENSIVE, 
    DEFENSIVE, 
    UTILITY, 
    DIAGNOSTICS,
    SYSTEM,
    UI_MOD,
    SECURITY
}

// ==========================================================================
// 2. PLUGIN MANIFEST (El ADN de cada módulo)
// ==========================================================================
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

// ==========================================================================
// 3. DASHBOARD WIDGET CONTRACT
// ==========================================================================
interface PluginWidgetProvider {
    @Composable
    fun Render(modifier: Modifier)
    fun onWidgetVisible()
    fun onWidgetHidden()
}

// ==========================================================================
// 4. THE OMNI PLUGIN INTERFACE (Contrato absoluto)
// ==========================================================================
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
