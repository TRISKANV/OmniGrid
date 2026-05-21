package com.tuapp.calculadora.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tuapp.calculadora.ui.system.RuntimeIntelligenceEngine

@Composable
fun OmniOSCockpit(modifier: Modifier = Modifier) {
    // Recolectamos el estado de optimización real en tiempo de ejecución
    val adaptationHint by RuntimeIntelligenceEngine.adaptationHint.collectAsState()
    val anomalies by RuntimeIntelligenceEngine.anomalies.collectAsState()

    Column(modifier = modifier.padding(16.dp)) {
        Text(
            text = "Hardware Engine Monitor", 
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Métricas de Adaptación Activas:", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(text = "• Reducir desenfoque (Blur): ${if (adaptationHint.reduceBlur) "ACTIVADO ⚠" else "DESACTIVADO"}")
                Text(text = "• Mitigar animaciones (Motion): ${if (adaptationHint.reduceMotion) "ACTIVADO ⚠" else "DESACTIVADO"}")
                Text(text = "• UI Simplificada (Rendering): ${if (adaptationHint.simplifyRendering) "FORZADO 🚨" else "NORMAL"}")
                Text(text = "• Delay de Muestreo Telemetría: ${adaptationHint.throttleTelemetryMs}ms")
            }
        }

        if (anomalies.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Anomalías Críticas Detectadas:", 
                style = MaterialTheme.typography.titleMedium, 
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(4.dp))
            
            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items(anomalies) { anomaly ->
                    Text(
                        text = "🚨 CORRUPCIÓN / ESTRÉS: $anomaly",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
