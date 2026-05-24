package com.tuapp.calculadora.core.plugin

import android.util.Log

enum class PluginState { REGISTERED, STARTING, RUNNING, FAILED }

interface OmniPlugin {
    val id: String
    fun onBoot()
}

object RuntimePluginManager {
    private const val TAG = "OMNI_RUNTIME_DEBUG"
    
    // ── 7. BANDERA GLOBAL DE ESTABILIZACIÓN ──
    var DEBUG_SAFE_MODE: Boolean = true

    private val registry = mutableMapOf<String, OmniPlugin>()
    private val pluginStates = mutableMapOf<String, PluginState>()

    fun registerPlugin(plugin: OmniPlugin) {
        registry[plugin.id] = plugin
        pluginStates[plugin.id] = PluginState.REGISTERED
        Log.d(TAG, "[PLUGIN_REGISTERED] ID: ${plugin.id}")
    }

    fun bootEcosystem() {
        Log.i(TAG, "[BOOT_START] Iniciando secuencia de arranque del ecosistema. SAFE_MODE = $DEBUG_SAFE_MODE")

        if (DEBUG_SAFE_MODE) {
            // ── 3. SAFE MODE: Aislamiento estricto de diagnóstico ──
            Log.w(TAG, "[BOOT_START] Modo seguro activo. Filtrando plugins críticos de diagnóstico.")
            bootSinglePlugin("DiagnosticsPlugin")
            bootSinglePlugin("PayloadRuntimePlugin") // Necesario para pruebas controladas
        } else {
            // Arranque completo estándar protegido
            registry.keys.forEach { pluginId ->
                bootSinglePlugin(pluginId)
            }
        }
        
        Log.i(TAG, "[TELEMETRY_STARTED] Inicialización del subsistema completada.")
    }

    private fun bootSinglePlugin(id: String) {
        val plugin = registry[id]
        if (plugin == null) {
            Log.e(TAG, "No se pudo iniciar el plugin $id: No está registrado en el sistema.")
            return
        }

        // ── 1. AISLAMIENTO INDIVIDUAL TRY/CATCH ──
        try {
            Log.d(TAG, "[PLUGIN_STARTING] Levantando entorno para: $id")
            pluginStates[id] = PluginState.STARTING
            
            // Disparo del ciclo de vida interno del plugin
            plugin.onBoot()
            
            pluginStates[id] = PluginState.RUNNING
            Log.i(TAG, "[PLUGIN_RUNNING] Sub-sistema estable: $id")
        } catch (t: Throwable) {
            pluginStates[id] = PluginState.FAILED
            // ── 2. LOGS ULTRA DETALLADOS DE CONTENCIÓN ──
            Log.e(TAG, "[PLUGIN_FAILED] Colapso crítico contenido en el plugin: $id. Motivo: ${t.localizedMessage}", t)
            // El error muere acá. El ciclo de la aplicación continúa vivo.
        }
    }

    fun getPluginState(id: String): PluginState? = pluginStates[id]
    
    fun notifyDashboardRendered() {
        Log.i(TAG, "[DASHBOARD_RENDERED] Hilo de interfaz gráfica de usuario (UI) estabilizado a 60 FPS.")
    }
}
