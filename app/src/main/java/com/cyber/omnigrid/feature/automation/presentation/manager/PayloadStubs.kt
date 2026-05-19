package com.cyber.omnigrid.feature.automation.presentation.manager

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel

class PayloadViewModel : ViewModel()

@Composable 
fun PayloadListScreen(viewModel: PayloadViewModel, onNavigateToEditor: (String) -> Unit) {}

@Composable 
fun PayloadEditorScreen(viewModel: PayloadViewModel, payloadId: String?, onNavigateBack: () -> Unit) {}
