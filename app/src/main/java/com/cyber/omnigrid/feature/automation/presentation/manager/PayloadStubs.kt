package com.cyber.omnigrid.feature.automation.presentation.manager

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

class PayloadViewModel(val repository: Any) : ViewModel() {
    init {
        Log.d("OMNI_BOOTSTRAP", "[VM_INIT] PayloadViewModel inicializado con dependencias.")
    }
}

@Composable 
fun PayloadListScreen(viewModel: PayloadViewModel, onNavigateToEditor: (String) -> Unit) {
    Log.d("OMNI_BOOTSTRAP", "[RENDER] Componiendo PayloadListScreen (Vista Auxiliar).")
    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFF151515)),
        contentAlignment = Alignment.Center
    ) {
        Text("STUB: OMNI PAYLOADS LIST", color = Color.Cyan, fontSize = 18.sp, fontFamily = FontFamily.Monospace)
    }
}

@Composable 
fun PayloadEditorScreen(viewModel: PayloadViewModel, payloadId: String?, onNavigateBack: () -> Unit) {
    Log.d("OMNI_BOOTSTRAP", "[RENDER] Componiendo PayloadEditorScreen (Vista Auxiliar).")
    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFF1E1E1E)),
        contentAlignment = Alignment.Center
    ) {
        Text("STUB: EDITOR ID -> $payloadId", color = Color.Yellow, fontSize = 18.sp, fontFamily = FontFamily.Monospace)
    }
}
