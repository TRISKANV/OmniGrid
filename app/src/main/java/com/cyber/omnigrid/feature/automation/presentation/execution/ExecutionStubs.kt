package com.cyber.omnigrid.feature.automation.presentation.execution

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel

class ExecutionViewModel : ViewModel()

@Composable 
fun LiveExecutionScreen(viewModel: ExecutionViewModel, scriptContent: String, onNavigateBack: () -> Unit) {}
