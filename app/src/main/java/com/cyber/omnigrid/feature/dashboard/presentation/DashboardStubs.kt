package com.cyber.omnigrid.feature.dashboard.presentation

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OmniDashboardScreen(
    onNavigateToLiveExecution: (String) -> Unit,
    onNavigateToSettings: () -> Unit
) {
    Log.d("OMNI_BOOTSTRAP", "[RENDER] Componiendo OmniDashboardScreen con éxito.")
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A))
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "BOOTSTRAP OK",
            color = Color(0xFF00FF66),
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "El pipeline visual de Compose está activo.\nSi ves esta pantalla, el NavHost inicializó de forma correcta.",
            color = Color.LightGray,
            fontSize = 14.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = { 
                Log.d("OMNI_BOOTSTRAP", "[NAV] Evento onClick: Solicitando navegación a la lista de Payloads.")
                onNavigateToLiveExecution("new_payload") 
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF222222))
        ) {
            Text(
                text = "Probar Enrutamiento (Ir a Payloads)", 
                color = Color.White, 
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
