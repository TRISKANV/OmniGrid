package com.cyber.omnigrid.feature.automation.presentation.execution

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel

class ExecutionViewModel : ViewModel() {
    init {
        Log.d("OMNI_BOOTSTRAP", "[VM_INIT] ExecutionViewModel inicializado.")
    }
}

@Composable 
fun LiveExecutionScreen(viewModel: ExecutionViewModel, scriptContent: String, onNavigateBack: () -> Unit) {
    Log.d("OMNI_BOOTSTRAP", "[RENDER] Componiendo LiveExecutionScreen (Vista Auxiliar).")
    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFF0D1B1E)),
        contentAlignment = Alignment.Center
    ) {
        Text("STUB: LIVE EXECUTION ENGINE", color = Color(0xFF00FF66), fontSize = 18.sp, fontFamily = FontFamily.Monospace)
    }
}
