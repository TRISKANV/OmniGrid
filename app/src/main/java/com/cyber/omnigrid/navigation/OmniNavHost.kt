package com.cyber.omnigrid.navigation

import android.util.Log
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.cyber.omnigrid.feature.automation.presentation.execution.ExecutionViewModel
import com.cyber.omnigrid.feature.automation.presentation.execution.LiveExecutionScreen
import com.cyber.omnigrid.feature.automation.presentation.manager.PayloadEditorScreen
import com.cyber.omnigrid.feature.automation.presentation.manager.PayloadListScreen
import com.cyber.omnigrid.feature.automation.presentation.manager.PayloadViewModel
import com.cyber.omnigrid.feature.dashboard.presentation.OmniDashboardScreen

@Composable
fun OmniNavHost(
    navController: NavHostController,
    payloadViewModel: PayloadViewModel,
    modifier: Modifier = Modifier
) {
    Log.d("OMNI_BOOTSTRAP", "[NAVHOST] Configurando Grafo. Destino inicial: ${Screen.Dashboard.route}")
    
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route,
        modifier = modifier,
        enterTransition = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300))
        },
        exitTransition = {
            fadeOut(animationSpec = tween(300))
        },
        popEnterTransition = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300))
        },
        popExitTransition = {
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
        }
    ) {
        
        // --- DASHBOARD FEATURE ---
        composable(route = Screen.Dashboard.route) {
            Log.d("OMNI_BOOTSTRAP", "[NAVHOST] Montando composable en ruta: ${Screen.Dashboard.route}")
            OmniDashboardScreen(
                onNavigateToLiveExecution = { actionType ->
                    Log.d("OMNI_BOOTSTRAP", "[NAVHOST] interceptado onNavigateToLiveExecution con argumento: $actionType")
                    if (actionType == "new_payload") {
                        navController.navigate(Screen.PayloadList.createRoute(workspaceId = "default_ws"))
                    }
                },
                onNavigateToSettings = { 
                    Log.d("OMNI_BOOTSTRAP", "[NAVHOST] interceptado onNavigateToSettings")
                }
            )
        }

        // --- AUTOMATION FEATURE (PAYLOADS) ---
        composable(
            route = Screen.PayloadList.route,
            arguments = listOf(navArgument("workspaceId") { type = NavType.StringType })
        ) { backStackEntry ->
            val workspaceId = backStackEntry.arguments?.getString("workspaceId") ?: "default_ws"
            Log.d("OMNI_BOOTSTRAP", "[NAVHOST] Montando PayloadList con workspaceId: $workspaceId")
            
            PayloadListScreen(
                viewModel = payloadViewModel,
                onNavigateToEditor = { payloadId ->
                    navController.navigate(Screen.PayloadEditor.createRoute(workspaceId, payloadId))
                }
            )
        }

        composable(
            route = Screen.PayloadEditor.route,
            arguments = listOf(
                navArgument("workspaceId") { type = NavType.StringType },
                navArgument("payloadId") { 
                    type = NavType.StringType 
                    nullable = true 
                    defaultValue = null 
                }
            )
        ) { backStackEntry ->
            val payloadId = backStackEntry.arguments?.getString("payloadId")
            Log.d("OMNI_BOOTSTRAP", "[NAVHOST] Montando PayloadEditor con payloadId: $payloadId")
            
            PayloadEditorScreen(
                viewModel = payloadViewModel,
                payloadId = payloadId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // --- LIVE EXECUTION FEATURE ---
        composable(
            route = Screen.LiveExecution.route,
            arguments = listOf(navArgument("payloadId") { type = NavType.StringType })
        ) { backStackEntry ->
            val payloadId = backStackEntry.arguments?.getString("payloadId") ?: ""
            Log.d("OMNI_BOOTSTRAP", "[NAVHOST] Montando LiveExecution con payloadId: $payloadId")
            
            val executionViewModel: ExecutionViewModel = viewModel()
            
            val demoScript = """
                DEFAULTDELAY 200
                REM Script de prueba activo
                GUI r
            """.trimIndent()
            
            LiveExecutionScreen(
                viewModel = executionViewModel,
                scriptContent = demoScript,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
