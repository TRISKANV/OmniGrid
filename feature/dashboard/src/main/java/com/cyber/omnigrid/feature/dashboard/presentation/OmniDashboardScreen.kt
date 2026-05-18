package com.cyber.omnigrid.feature.dashboard.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cyber.omnigrid.core.designsystem.theme.TrueBlack
import com.cyber.omnigrid.feature.dashboard.presentation.components.*

@Composable
fun OmniDashboardScreen(
    onNavigateToLiveExecution: (String) -> Unit,
    onNavigateToSettings: () -> Unit
) {
    // Scaffold nos da el esqueleto base. El fondo es TrueBlack puro.
    Scaffold(
        containerColor = TrueBlack,
        topBar = {
            // Header que maneja el cambio de Workspaces
            WorkspaceHeader(
                currentWorkspace = "RED TEAM LAB",
                onWorkspaceClick = { /* Abrir Dropdown o BottomSheet */ }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // 1. Estado de los sistemas críticos y bóveda de seguridad
            SystemStatusWidget()

            // 2. Acciones de un solo toque (El pipeline visual)
            QuickActionsSection(onActionClick = onNavigateToLiveExecution)

            // 3. Tarjetas de categorías/módulos (Rucky, OSINT, Network)
            ModulesGrid()

            // 4. Logs de actividad reciente
            RecentActivityList()
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
