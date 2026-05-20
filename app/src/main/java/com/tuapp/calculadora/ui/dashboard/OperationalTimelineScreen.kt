package com.tuapp.calculadora.ui.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tuapp.calculadora.ui.system.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun OperationalTimelineScreen() {
    val logs by RuntimeTelemetryManager.logs.collectAsState()
    val adaptive = LocalAdaptiveConfig.current

    Column(
        modifier = Modifier.fillMaxSize().background(Color.Transparent).padding(16.dp)
    ) {
        Text(
            text = "OPERATIONAL TIMELINE",
            color = Color(0xFF00FF66),
            fontSize = 18.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.sp
        )
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            itemsIndexed(logs) { index, log ->
                val isLast = index == logs.lastIndex
                TimelineNode(log = log, isLast = isLast, simplify = adaptive.renderComplexity < 80)
            }
        }
    }
}

@Composable
private fun TimelineNode(log: LogEntry, isLast: Boolean, simplify: Boolean) {
    val nodeColor = when (log.level) {
        LogLevel.INFO -> Color(0xFF00FF66)
        LogLevel.WARN -> Color(0xFFFFCC00)
        LogLevel.CRITICAL -> Color(0xFFFF3333)
        LogLevel.EXEC -> Color(0xFF00E5FF)
    }

    Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
        // Track & Node Drawing (GPU Canvas)
        Canvas(modifier = Modifier.width(32.dp).fillMaxHeight()) {
            val strokeW = 1.5.dp.toPx()
            val nodeRadius = if (simplify) 3.dp.toPx() else 4.dp.toPx()
            
            // Línea vertical
            if (!isLast) {
                drawLine(
                    color = Color(0xFF222222),
                    start = Offset(size.width / 2, 0f),
                    end = Offset(size.width / 2, size.height),
                    strokeWidth = strokeW
                )
            }
            // Glow y Nodo central
            if (!simplify) {
                drawCircle(color = nodeColor.copy(alpha = 0.2f), radius = nodeRadius * 2.5f, center = Offset(size.width / 2, 24.dp.toPx()))
            }
            drawCircle(color = nodeColor, radius = nodeRadius, center = Offset(size.width / 2, 24.dp.toPx()))
        }

        // Event Payload
        Column(modifier = Modifier.padding(top = 12.dp, bottom = 16.dp)) {
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Text(
                    text = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date(log.timestamp)),
                    color = Color.DarkGray,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = log.tag,
                    color = nodeColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = log.message,
                color = Color.LightGray,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                lineHeight = 16.sp
            )
        }
    }
}
