package com.tuapp.calculadora.core.plugin

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.omnigrid.payload.domain.repository.PayloadRepository
import com.omnigrid.payload.domain.usecase.*
import com.omnigrid.payload.presentation.ui.screens.*
import com.omnigrid.payload.presentation.viewmodel.PayloadManagerViewModel // ⚠️ IMPORT EXPLÍCITO FALTANTE
import com.omnigrid.payload.presentation.viewmodel.PayloadEditorViewModel // ⚠️ IMPORT EXPLÍCITO FALTANTE
import com.omnigrid.payload.presentation.viewmodel.PayloadTerminalViewModel // ⚠️ IMPORT EXPLÍCITO FALTANTE
import com.omnigrid.payload.runtime.engine.DuckyRuntimeEngine
import com.omnigrid.payload.runtime.session.RuntimeSessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// Máquina de estados para la navegación interna del Widget
sealed class PayloadWidgetRoute {
    object Manager : PayloadWidgetRoute()
    data class Editor(val payloadId: String?) : PayloadWidgetRoute()
    data class Terminal(val sessionId: String) : PayloadWidgetRoute()
}

class PayloadRuntimePlugin(
    private val repository: PayloadRepository,
    private val sessionManager: RuntimeSessionManager,
    private val engine: DuckyRuntimeEngine,
    private val getPayloads: GetPayloadsUseCase,
    private val createPayload: CreatePayloadUseCase,
    private val updatePayload: UpdatePayloadUseCase,
    private val deletePayload: DeletePayloadUseCase,
    private val executePayload: ExecutePayloadUseCase,
    private val manageSession: ManageSessionUseCase
) : OmniPlugin {

    override val manifest = PluginManifest(
        pluginId = "core.tactical.payloads",
        version = "1.0.0",
        capabilities = setOf(PluginCapability.PAYLOAD_EXECUTION, PluginCapability.UI_DASHBOARD_WIDGET),
        priority = 95
    )

    private val _state = MutableStateFlow(PluginState.IDLE)
    override val state: StateFlow<PluginState> = _state.asStateFlow()

    private val currentRoute = MutableStateFlow<PayloadWidgetRoute>(PayloadWidgetRoute.Manager)

    override suspend fun initialize() { _state.value = PluginState.INITIALIZING }
    override suspend fun start() { _state.value = PluginState.RUNNING }
    override suspend fun stop() { _state.value = PluginState.DISABLED }
    override suspend fun recover() { stop(); currentRoute.value = PayloadWidgetRoute.Manager; start() }

    @Composable
    override fun RenderWidget() {
        val route by currentRoute.collectAsState()

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 400.dp, max = 800.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF030303))
                .border(1.dp, Color(0xFF1E1E2E), RoundedCornerShape(8.dp))
        ) {
            Crossfade(targetState = route, label = "PayloadRouter") { currentScreen ->
                when (currentScreen) {
                    is PayloadWidgetRoute.Manager -> {
                        val viewModel = remember {
                            PayloadManagerViewModel(getPayloads, createPayload, updatePayload, deletePayload, executePayload, manageSession, sessionManager)
                        }
                        PayloadManagerScreen(
                            viewModel = viewModel,
                            onNavigateToTerminal = { currentRoute.value = PayloadWidgetRoute.Terminal(it) },
                            onNavigateToEditor = { currentRoute.value = PayloadWidgetRoute.Editor(it) }
                        )
                    }
                    is PayloadWidgetRoute.Editor -> {
                        val viewModel = remember(currentScreen.payloadId) {
                            PayloadEditorViewModel(createPayload, updatePayload, repository, currentScreen.payloadId)
                        }
                        PayloadEditorScreen(
                            viewModel = viewModel,
                            onBack = { currentRoute.value = PayloadWidgetRoute.Manager },
                            onSaved = { currentRoute.value = PayloadWidgetRoute.Manager }
                        )
                    }
                    is PayloadWidgetRoute.Terminal -> {
                        val viewModel = remember(currentScreen.sessionId) {
                            PayloadTerminalViewModel(sessionManager, repository, engine, currentScreen.sessionId)
                        }
                        PayloadTerminalScreen(
                            viewModel = viewModel,
                            sessionId = currentScreen.sessionId,
                            onBack = { currentRoute.value = PayloadWidgetRoute.Manager }
                        )
                    }
                }
            }
        }
    }
}
