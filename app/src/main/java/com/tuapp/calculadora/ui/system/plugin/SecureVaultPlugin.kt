package com.tuapp.calculadora.ui.system.plugin

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tuapp.calculadora.ui.system.*
import com.tuapp.calculadora.ui.system.sdk.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID
import kotlin.random.Random

// ==========================================================================
// SECURE VAULT DOMAIN & METRICS STATE
// ==========================================================================
enum class VaultState { LOCKED, UNLOCKED, DEGRADED_PROCESSING }

data class VaultMetrics(
    val storageUsedBytes: Long = 149520384L, // ~142.6 MB Iniciales
    val storageMaxBytes: Long = 1073741824L,  // 1 GB Max Vault Allocation
    val currentThroughputMbs: Double = 0.0,
    val averageLatencyMs: Long = 12,
    val integrityScore: Float = 1.0f,
    val activeSecureSessions: Int = 1
)

// ==========================================================================
// OMNIPLUGIN IMPLEMENTATION
// ==========================================================================
class SecureVaultPlugin : OmniPlugin {

    private val _vaultState = MutableStateFlow(VaultState.LOCKED)
    val vaultState: StateFlow<VaultState> = _vaultState.asStateFlow()

    private val _metrics = MutableStateFlow(VaultMetrics())
    val metrics: StateFlow<VaultMetrics> = _metrics.asStateFlow()

    private val _adaptiveRefreshRateMs = MutableStateFlow(1000L) // 1Hz por defecto

    private var pluginScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    override val manifest = PluginManifest(
        pluginId = "secure.vault.crypto",
        displayName = "SECURE STORAGE VAULT",
        version = "2.1.0",
        description = "Módulo criptográfico avanzado de almacenamiento aislado con aceleración por hardware",
        category = PluginCategory.SECURITY,
        providedCapabilities = setOf(SystemCapability.SECURE_STORAGE, SystemCapability.CRYPTO_ACCELERATION),
        consumedCapabilities = setOf(SystemCapability.HARDWARE_TELEMETRY),
        requiredPermissions = listOf("android.permission.USE_BIOMETRIC", "android.permission.MANAGE_EXTERNAL_STORAGE"),
        visualPriority = 0, // Prioridad Máxima (Ancho completo en grilla)
        supportsHeadlessExecution = false,
        transportCompatibility = listOf("LOCAL", "IPC")
    )

    override val widgetProvider = object : PluginWidgetProvider {
        @Composable
        override fun Render(modifier: Modifier) {
            SecureVaultWidget(this@SecureVaultPlugin, modifier)
        }
        override fun onWidgetVisible() {
            // Activar telemetría caliente si el widget está en pantalla
            logRuntimeEvent("METRICS_STREAM_FOCUS", "Widget de bóveda visible en el HUD.")
        }
        override fun onWidgetHidden() {
            // Reducir consumo si no se está visualizando
            logRuntimeEvent("METRICS_STREAM_IDLE", "Widget oculto, reduciendo ciclo de refresco secundario.")
        }
    }

    override fun onInstall() {
        logRuntimeEvent("VAULT_PROVISIONED", "Estructuras criptográficas del almacenamiento inicializadas correctamente.")
    }

    override fun onBoot() {
        pluginScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
        _vaultState.value = VaultState.LOCKED
        
        // Escuchar reactivamente al RuntimeIntelligenceEngine para aplicar Throttling y Comportamiento Adaptativo
        pluginScope.launch {
            RuntimeIntelligenceEngine.anomalies.collect { anomalies ->
                if (anomalies.isNotEmpty()) {
                    // Estado crítico detectado por la IA -> Actuar de inmediato de forma autónoma
                    _adaptiveRefreshRateMs.value = 3000L // Relajar refresco de UI a 3s para liberar la GPU
                    if (_vaultState.value == VaultState.UNLOCKED) {
                        _vaultState.value = VaultState.DEGRADED_PROCESSING
                    }
                    logRuntimeEvent("VAULT_ADAPTIVE_THROTTLE", "Fricción térmica o anomalía del sistema detectada. Entrando en modo de protección de batería/recomposición reducida.")
                } else {
                    _adaptiveRefreshRateMs.value = 1000L // Volver a frecuencia nominal
                    if (_vaultState.value == VaultState.DEGRADED_PROCESSING) {
                        _vaultState.value = VaultState.UNLOCKED
                    }
                }
            }
        }

        // Loop Simulado de operaciones en segundo plano y telemetría de procesamiento en vivo
        pluginScope.launch {
            while (isActive) {
                if (_vaultState.value != VaultState.LOCKED) {
                    val isDegraded = _adaptiveRefreshRateMs.value > 1000L
                    val simulatedThroughput = if (isDegraded) Random.nextDouble(0.5, 2.1) else Random.nextDouble(12.4, 48.9)
                    val simulatedLatency = if (isDegraded) Random.nextLong(45, 90) else Random.nextLong(8, 16)
                    
                    _metrics.value = _metrics.value.copy(
                        currentThroughputMbs = simulatedThroughput,
                        averageLatencyMs = simulatedLatency,
                        storageUsedBytes = _metrics.value.storageUsedBytes + if (!isDegraded) Random.nextLong(1024, 8192) else 0L
                    )

                    // Publicar métricas crudas en el Bus Central
                    if (!isDegraded || Random.nextFloat() > 0.7f) { // Omitir telemetría secundaria si está bajo estrés
                        CoreEventBus.emit(SystemEvent("VAULT_IO_METRICS", mapOf(
                            "throughput" to simulatedThroughput,
                            "latency" to simulatedLatency,
                            "allocated_bytes" to _metrics.value.storageUsedBytes
                        )))
                    }
                }
                delay(_adaptiveRefreshRateMs.value)
            }
        }

        logRuntimeEvent("VAULT_BOOT_SUCCESS", "Módulo SecureVaultPlugin enlazado al ecosistema Omni_OS.")
    }

    override fun onSuspend() {
        _vaultState.value = VaultState.LOCKED
        pluginScope.cancel()
        logRuntimeEvent("VAULT_SUSPENDED", "Bóveda cerrada preventivamente y corrutinas suspendidas.")
    }

    override fun onDestroy() {
        pluginScope.cancel()
        logRuntimeEvent("VAULT_TERMINATED", "Desvinculación absoluta del hardware de cifrado seguro.")
    }

    override fun executeAction(actionId: String, payload: Map<String, Any>): Result<Unit> {
        return when(actionId) {
            "ACTION_UNLOCK" -> {
                _vaultState.value = VaultState.UNLOCKED
                logRuntimeEvent("VAULT_UNLOCK_SUCCESS", "Acceso concedido a la bóveda cifrada mediante firma de sesión única.")
                Result.success(Unit)
            }
            "ACTION_LOCK" -> {
                _vaultState.value = VaultState.LOCKED
                logRuntimeEvent("VAULT_LOCK_COMMAND", "Cierre explícito de la zona de almacenamiento seguro solicitado por el orquestador.")
                Result.success(Unit)
            }
            "INJECT_ANOMALY" -> {
                _metrics.value = _metrics.value.copy(integrityScore = 0.82f)
                logRuntimeEvent("VAULT_INTEGRITY_WARN", "Alerta estructural: Solicitudes de descifrado con firmas corruptas detectadas.")
                Result.success(Unit)
            }
            else -> Result.failure(IllegalArgumentException("Acción criptográfica '$actionId' no reconocida."))
        }
    }

    override fun getHealthStatus(): String {
        val score = _metrics.value.integrityScore
        return when {
            score >= 1.0f -> "NOMINAL"
            score > 0.8f -> "DEGRADED"
            else -> "COMPROMISED"
        }
    }

    private fun logRuntimeEvent(type: String, description: String) {
        // Registro centralizado e inyección automática en la Operational Timeline
        CoreEventBus.emit(SystemEvent(type, mapOf("description" to description, "origin" to manifest.pluginId)))
    }
}

// ==========================================================================
// TACTICAL PRESENTATION LAYER (HUD WIDGETS)
// ==========================================================================
@Composable
fun SecureVaultWidget(plugin: SecureVaultPlugin, modifier: Modifier) {
    val state by plugin.vaultState.collectAsState()
    val metrics by plugin.metrics.collectAsState()

    // Animación GPU de latido táctico para indicar salud de cifrado
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(4.dp)
    ) {
        // Fila de Encabezado de Estado Criptográfico
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .alpha(if (state != VaultState.LOCKED) pulseAlpha else 1f)
                        .background(
                            when (state) {
                                VaultState.LOCKED -> Color(0xFFFF3333)
                                VaultState.UNLOCKED -> Color(0xFF00FF66)
                                VaultState.DEGRADED_PROCESSING -> Color(0xFFFFB300)
                            },
                            shape = RoundedCornerShape(50)
                        )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "VAULT_STATE: ${state.name}",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "INTEGRITY: ${(metrics.integrityScore * 100).toInt()}%",
                color = if (metrics.integrityScore >= 1.0f) Color(0xFF00E5FF) else Color(0xFFFF3333),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Barra de Almacenamiento OLED / Glass
        val usedMb = metrics.storageUsedBytes / (1024f * 1024f)
        val maxMb = metrics.storageMaxBytes / (1024f * 1024f)
        val fillPct = (usedMb / maxMb).coerceIn(0f, 1f)

        Column(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = "ALLOCATED STORAGE CAPACITY",
                    color = Color.Gray,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "${String.format("%.1f", usedMb)}MB / ${maxMb.toInt()}MB",
                    color = Color.LightGray,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(Color(0------------222222), RoundedCornerShape(2.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(fillPct)
                        .background(Color(0xFF00E5FF), RoundedCornerShape(2.dp))
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Grid de Métricas de Rendimiento en Tiempo Real (I/O)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("CRYPTO_THROUGHPUT", color = Color.Gray, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                Text(
                    text = "${String.format("%.2f", metrics.currentThroughputMbs)} MB/s",
                    color = if (metrics.currentThroughputMbs > 0.0) Color(0xFF00FF66) else Color.DarkGray,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
            Column(alignment = Alignment.End) {
                Text("HARDWARE_LATENCY", color = Color.Gray, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                Text(
                    text = "${metrics.averageLatencyMs} ms",
                    color = when {
                        metrics.averageLatencyMs <= 15 -> Color(0xFF00FF66)
                        metrics.averageLatencyMs <= 40 -> Color(0xFFFFB300)
                        else -> Color(0xFFFF3333)
                    },
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Fila de Control Criptográfico Rápido (Solo visible en simulación)
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            // Acciones emuladas inter-plugin a través de disparadores tácticos
            Box(
                modifier = Modifier
                    .background(Color(0xFF1E1E1E), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "TAP TO TOGGLE VAULT",
                    color = Color.DarkGray,
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
