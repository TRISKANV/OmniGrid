package com.tuapp.calculadora.ui.system.sdk

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

// ==========================================================================
// 1. CAPABILITY-DRIVEN PLATFORM
// Los plugins ya no hacen "cosas", exponen y consumen capacidades.
// ==========================================================================
enum class SystemCapability {
    HID_INJECTION,
    NETWORK_OBSERVABILITY,
    HARDWARE_TELEMETRY,
    SECURE_ENCRYPTION,
    RUNTIME_EXECUTION,
    TRANSPORT_MESH,
    SIGNAL_INTELLIGENCE
}

enum class PluginCategory {
    SYSTEM_CORE, TELEMETRY, OFFENSIVE, DEFENSIVE, UTILITY, DIAGNOSTICS
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
    val visualPriority: Int, // 0 = Core/Top, 100 = Background
    val supportsHeadlessExecution: Boolean,
    val transportCompatibility: List<String> // ej: ["LOCAL", "USB_OTG", "BT_SERIAL"]
)

// ==========================================================================
// 3. DASHBOARD WIDGET CONTRACT
// ==========================================================================
interface PluginWidgetProvider {
    @Composable
    fun Render(modifier: Modifier)
    
    // Hooks para control de recomposición y GPU
    fun onWidgetVisible()
    fun onWidgetHidden()
}

// ==========================================================================
// 4. THE OMNI PLUGIN INTERFACE (Contrato absoluto)
// ==========================================================================
interface OmniPlugin {
    val manifest: PluginManifest
    val widgetProvider: PluginWidgetProvider?

    // Lifecycle Callbacks del O.S.
    fun onInstall()
    fun onBoot()
    fun onSuspend()
    fun onDestroy()

    // Execution Contracts
    fun executeAction(actionId: String, payload: Map<String, Any>): Result<Any>
    
    // Diagnostics & Telemetry Hooks
    fun getHealthStatus(): String
}
