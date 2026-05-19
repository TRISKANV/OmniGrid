package com.cyber.omnigrid.feature.automation.presentation.manager

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel

// Le agregamos el parámetro al constructor que exige la MainActivity
class PayloadViewModel(val repository: Any) : ViewModel()

@Composable 
fun PayloadListScreen(viewModel: PayloadViewModel, onNavigateToEditor: (String) -> Unit) {}

@Composable 
fun PayloadEditorScreen(viewModel: PayloadViewModel, payloadId: String?, onNavigateBack: () -> Unit) {}
