package com.tuapp.calculadora.core.plugin

import com.tuapp.calculadora.core.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Sandbox de aislamiento. Contiene la instancia, su límite de errores y su scope aislado.
 */
data class PluginSandbox(
    val plugin: OmniPlugin,
    var scope: CoroutineScope,
    var crashCount: Int = 0,
    val maxRetries: Int = 3
)

object RuntimePluginManager {
    // El Orquestador Maestro. Si un hijo falla, el supervisor no se cancela.
    private val kernelScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    // Registry de Sandboxes
    private val _registry = ConcurrentHashMap<String, PluginSandbox>()

    // Pipeline dinámico hacia la UI
    private val _activePlugins = MutableStateFlow<List<OmniPlugin>>(emptyList())
    val activePlugins: StateFlow<List<OmniPlugin>> = _activePlugins.asStateFlow()

    fun registerPlugin(plugin: OmniPlugin) {
        if (_registry.containsKey(plugin.manifest.pluginId)) return
        
        // Asignamos un Sandbox inactivo inicialmente
        _registry[plugin.manifest.pluginId] = PluginSandbox(
            plugin = plugin,
            scope = createIsolatedScope(plugin)
        )
        publishKernelEvent("REGISTRY", "Registered: ${plugin.manifest.pluginId}")
    }

    fun bootEcosystem() {
        kernelScope.launch {
            val sortedSandboxes = _registry.values.sortedByDescending { it.plugin.manifest.priority }
            publishKernelEvent("BOOT", "Igniting Micro-Kernel with ${sortedSandboxes.size} modules...")

            for (sandbox in sortedSandboxes) {
                launchInSandbox(sandbox)
            }
            
            startHealthMonitor()
        }
    }

    /**
     * Lanza el plugin dentro de su espacio seguro.
     */
    private fun launchInSandbox(sandbox: PluginSandbox) {
        val pluginId = sandbox.plugin.manifest.pluginId
        
        sandbox.scope.launch {
            try {
                publishKernelEvent("INIT", "Starting [$pluginId]")
                sandbox.plugin.initialize()
                sandbox.plugin.start()
                updateUIState()
            } catch (e: CancellationException) {
                // Cancelaciones normales de corrutinas (no son crashes)
                throw e
            } catch (e: Exception) {
                // Captura fallos directos en la inicialización
                handleCrash(sandbox, e)
            }
        }
    }

    /**
     * Crea un scope cerrado para el plugin. Si el scope revienta, el exception handler avisa al Kernel.
     */
    private fun createIsolatedScope(plugin: OmniPlugin): CoroutineScope {
        val handler = CoroutineExceptionHandler { _, throwable ->
            val sandbox = _registry[plugin.manifest.pluginId] ?: return@CoroutineExceptionHandler
            handleCrash(sandbox, throwable)
        }
        return CoroutineScope(SupervisorJob() + Dispatchers.Default + handler)
    }

    /**
     * CRASH ISOLATION & RECOVERY SYSTEM
     */
    private fun handleCrash(sandbox: PluginSandbox, throwable: Throwable) {
        val pluginId = sandbox.plugin.manifest.pluginId
        sandbox.crashCount++

        kernelScope.launch {
            CoreEventBus.publish(HardwareWarning(
                message = "FATAL FAULT in [$pluginId] | Attempt ${sandbox.crashCount}/${sandbox.maxRetries} | Err: ${throwable.message}",
                subsystem = "SANDBOX",
                severity = "CRITICAL"
            ))

            // Mata el scope actual corrupto del plugin
            sandbox.scope.cancel()

            if (sandbox.crashCount > sandbox.maxRetries) {
                publishKernelEvent("EJECT", "Module [$pluginId] has been PERMANENTLY DISABLED.")
                sandbox.plugin.stop() // Forzamos apagado final
                updateUIState()
                return@launch
            }

            // Recovery Policy
            publishKernelEvent("RECOVERY", "Attempting restart of [$pluginId]...")
            sandbox.plugin.recover()
            
            // Asignamos un nuevo Scope limpio y relanzamos
            sandbox.scope = createIsolatedScope(sandbox.plugin)
            launchInSandbox(sandbox)
        }
    }

    /**
     * HEALTH MONITORING (Watchdog)
     */
    private suspend fun startHealthMonitor() {
        while (kernelScope.isActive) {
            delay(5000L)
            updateUIState()
        }
    }

    private fun updateUIState() {
        // El Dashboard solo debe ver plugins vivos o en estado intermedio
        _activePlugins.value = _registry.values
            .map { it.plugin }
            .filter { 
                (it.state.value == PluginState.RUNNING || it.state.value == PluginState.DEGRADED || it.state.value == PluginState.RECOVERING) &&
                it.manifest.capabilities.contains(PluginCapability.UI_DASHBOARD_WIDGET)
            }
            .sortedByDescending { it.manifest.priority }
    }

    private fun publishKernelEvent(tag: String, msg: String) {
        kernelScope.launch {
            CoreEventBus.publish(PluginSystemEvent(type = tag, payload = msg))
        }
    }
}
