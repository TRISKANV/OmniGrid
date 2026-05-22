package com.tuapp.calculadora.core.plugin

import com.tuapp.calculadora.core.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.concurrent.ConcurrentHashMap

object RuntimePluginManager {
    // SupervisorJob garantiza que si un plugin crashea, el Manager y el resto del OS siguen vivos.
    private val orchestratorScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    // Registro interno inmutable en runtime
    private val _registry = ConcurrentHashMap<String, OmniPlugin>()
    private val pluginJobs = ConcurrentHashMap<String, Job>()

    // Estado reactivo puro para el Dashboard Dinámico
    private val _activePlugins = MutableStateFlow<List<OmniPlugin>>(emptyList())
    val activePlugins: StateFlow<List<OmniPlugin>> = _activePlugins.asStateFlow()

    fun registerPlugin(plugin: OmniPlugin) {
        if (_registry.containsKey(plugin.manifest.pluginId)) return
        _registry[plugin.manifest.pluginId] = plugin
        publishSystemEvent("REGISTRY", "Plugin registered: ${plugin.manifest.pluginId}")
    }

    /**
     * Bootloader del Ecosistema: Levanta los módulos resolviendo prioridades.
     */
    fun bootEcosystem() {
        orchestratorScope.launch {
            val sortedPlugins = _registry.values.sortedByDescending { it.manifest.priority }
            publishSystemEvent("BOOTLOADER", "Igniting ${sortedPlugins.size} registered plugins...")

            for (plugin in sortedPlugins) {
                launchPlugin(plugin)
            }
            
            // Inicia ciclo de monitoreo de salud (Runtime Health)
            startHealthMonitor()
        }
    }

    private fun launchPlugin(plugin: OmniPlugin) {
        // Isolation Layer: Atrapa excepciones fatales dentro del hilo del plugin
        val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
            handlePluginCrash(plugin, throwable)
        }

        val job = orchestratorScope.launch(exceptionHandler) {
            publishSystemEvent("INIT", "Initializing [${plugin.manifest.pluginId}]")
            
            plugin.initialize()
            plugin.start()
            
            updateActivePluginsUI()
        }
        
        pluginJobs[plugin.manifest.pluginId] = job
    }

    private fun handlePluginCrash(plugin: OmniPlugin, throwable: Throwable) {
        // Alerta crítica de aislamiento, sin tirar el OS completo
        orchestratorScope.launch {
            CoreEventBus.publish(HardwareWarning(
                message = "CRITICAL PLUGIN FAULT: ${plugin.manifest.pluginId} | ${throwable.message}",
                subsystem = "KERNEL_ISOLATION",
                severity = "CRITICAL"
            ))
            
            // Intento de recuperación / degradación elegante
            try {
                plugin.stop()
                plugin.recover()
            } catch (e: Exception) {
                publishSystemEvent("KERNEL_PANIC", "Module [${plugin.manifest.pluginId}] permanently isolated.")
            }
            
            updateActivePluginsUI()
        }
    }

    private suspend fun startHealthMonitor() {
        while (isActive) {
            delay(5000L) // Scan de salud cada 5 segundos
            updateActivePluginsUI()
        }
    }

    private fun updateActivePluginsUI() {
        // Filtra y expone al Dashboard ÚNICAMENTE los plugins vivos/degradados que tengan interfaz gráfica
        _activePlugins.value = _registry.values
            .filter { 
                (it.state.value == PluginState.ACTIVE || it.state.value == PluginState.DEGRADED) &&
                it.manifest.capabilities.contains(PluginCapability.UI_DASHBOARD_WIDGET)
            }
            .sortedByDescending { it.manifest.priority }
    }

    private fun publishSystemEvent(tag: String, msg: String) {
        orchestratorScope.launch {
            CoreEventBus.publish(PluginSystemEvent(type = tag, payload = msg))
        }
    }
}
