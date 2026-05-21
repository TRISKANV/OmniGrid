package com.tuapp.calculadora.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tuapp.calculadora.ui.system.RuntimeIntelligenceEngine
import com.tuapp.calculadora.ui.system.model.RuntimeSignal
import kotlinx.coroutines.flow.collectLatest

@Composable
fun OperationalTimelineScreen(modifier: Modifier = Modifier) {
    // Almacén local reactivo para el histórico de eventos del ciclo de vida del hardware
    val logsTimeline = remember { mutableStateListOf<String>() }

    // Escucha activa del SharedFlow del motor core
    LaunchedEffect(Unit) {
        RuntimeIntelligenceEngine.signals.collectLatest { signal ->
            when (signal) {
                is RuntimeSignal.Warning -> {
                    logsTimeline.add(0, "[WARN - ${signal.level}]: ${signal.message}")
                }
                is RuntimeSignal.PerformanceHint -> {
                    logsTimeline.add(0, "[HINT]: Telemetría reajustada a ${signal.throttleTelemetryMs}ms")
                }
            }
        }
    }

    Column(modifier = modifier.padding(16.dp)) {
        Text(text = "Línea de Tiempo Operacional", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        if (logsTimeline.isEmpty()) {
            Text(
                text = "No hay eventos registrados en este ciclo. Sistema operativo nominal.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(
                    items = logsTimeline,
                    key = { index, item -> "$index-$item" }
                ) { _, logMessage ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                        )
                    ) {
                        Text(
                            text = logMessage,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}
