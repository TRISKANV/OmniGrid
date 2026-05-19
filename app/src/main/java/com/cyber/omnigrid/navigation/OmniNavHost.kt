package com.cyber.omnigrid.navigation

import android.util.Log
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
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
    val motionSpec = spring<androidx.compose.ui.unit.IntOffset>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessLow
    )
    val fadeSpec = spring<Float>(stiffness = Spring.StiffnessLow)

    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route,
        modifier = modifier,
        enterTransition = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = motionSpec) + fadeIn(animationSpec = fadeSpec)
        },
        exitTransition = { fadeOut(animationSpec = fadeSpec) },
        popEnterTransition = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = motionSpec) + fadeIn(animationSpec = fadeSpec)
        },
        popExitTransition = {
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = motionSpec) + fadeOut(animationSpec = fadeSpec)
        }
    ) {
        // --- DASHBOARD ---
        composable(route = Screen.Dashboard.route) {
            OmniDashboardScreen(
                onNavigateToLiveExecution = { actionType ->
                    if (actionType == "new_payload") {
                        navController.navigate(Screen.PayloadList.createRoute(workspaceId = "default_ws"))
                    }
                },
                onNavigateToSettings = { }
            )
        }

        // --- AUTOMATION (PAYLOADS) ---
        composable(
            route = Screen.PayloadList.route,
            arguments = listOf(navArgument("workspaceId") { type = NavType.StringType })
        ) { backStackEntry ->
            val workspaceId = backStackEntry.arguments?.getString("workspaceId") ?: "default_ws"
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
                navArgument("payloadId") { type = NavType.StringType; nullable = true; defaultValue = null }
            )
        ) { backStackEntry ->
            val payloadId = backStackEntry.arguments?.getString("payloadId")
            PayloadEditorScreen(
                viewModel = payloadViewModel,
                payloadId = payloadId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // --- LIVE EXECUTION ---
        composable(
            route = Screen.LiveExecution.route,
            arguments = listOf(navArgument("payloadId") { type = NavType.StringType })
        ) { backStackEntry ->
            val executionViewModel: ExecutionViewModel = viewModel()
            LiveExecutionScreen(
                viewModel = executionViewModel,
                scriptContent = "DEFAULTDELAY 200\nREM OmniGrid Live\nGUI r",
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
