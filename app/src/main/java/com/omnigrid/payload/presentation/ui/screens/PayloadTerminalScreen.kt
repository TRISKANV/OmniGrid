package com.omnigrid.payload.presentation.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.omnigrid.payload.domain.model.*

// ⚠️ IMPORTS EXPLÍCITOS AGREGADOS AQUÍ ⚠️
import com.omnigrid.payload.presentation.viewmodel.PayloadTerminalViewModel
import com.omnigrid.payload.presentation.viewmodel.LiveMetrics
import com.omnigrid.payload.presentation.viewmodel.LiveTerminalEntry
import com.omnigrid.payload.presentation.viewmodel.TerminalCategory

@Composable
fun PayloadTerminalScreen(
    viewModel: PayloadTerminalViewModel,
    sessionId: String,
    onBack: () -> Unit
) {
    val session by viewModel.session.collectAsState()
    val liveEvents by viewModel.liveEvents.collectAsState()
    val metrics by viewModel.liveMetrics.collectAsState()
    val autoScroll by viewModel.autoScroll.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(liveEvents.size) {
        if (autoScroll && liveEvents.isNotEmpty()) {
            listState.animateScrollToItem(liveEvents.size - 1)
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(OmniColors.background)) {
        Column(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
            TerminalHeader(session = session, onBack = onBack, onCancel = { viewModel.cancelSession() })
            SessionMetricsHUD(metrics = metrics)
            ExecutionProgressBar(progress = metrics.progress, state = metrics.state)
            HorizontalDivider(color = OmniColors.border, thickness = 1.dp)

            Box(modifier = Modifier.weight(1f)) {
                LiveTerminal(events = liveEvents, listState = listState, onScrollChanged = { viewModel.setAutoScroll(it) })
                if (!autoScroll) {
                    FloatingActionButton(onClick = { viewModel.setAutoScroll(true) }, modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp).size(36.dp), containerColor = OmniColors.surfaceElevated, contentColor = OmniColors.primary) {
                        Icon(Icons.Default.KeyboardArrowDown, "Auto scroll", modifier = Modifier.size(18.dp))
                    }
                }
            }

            TerminalBottomBar(session = session, onClear = { viewModel.clearLiveEvents() }, onCancel = { viewModel.cancelSession() })
        }
    }
}

@Composable
private fun TerminalHeader(session: PayloadSession?, onBack: () -> Unit, onCancel: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconButton(onClick = onBack, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.ArrowBack, "Back", tint = OmniColors.textSecondary, modifier = Modifier.size(18.dp)) }
            Column {
                Text("RUNTIME TERMINAL", color = OmniColors.primary, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 2.sp)
                Text(session?.sessionId?.take(12)?.uppercase() ?: "NO SESSION", color = OmniColors.textSecondary, fontFamily = FontFamily.Monospace, fontSize = 10.sp)
            }
        }
        session?.let { StateIndicator(state = it.state) }
    }
    HorizontalDivider(color = OmniColors.border)
}

@Composable
fun SessionMetricsHUD(metrics: LiveMetrics) {
    Row(modifier = Modifier.fillMaxWidth().background(OmniColors.surface).padding(horizontal = 16.dp, vertical = 10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        MetricCell("ACTIONS", "${metrics.completedActions}/${metrics.totalActions}", OmniColors.primary)
        MetricDivider()
        MetricCell("FAILED", "${metrics.failedActions}", if (metrics.failedActions > 0) OmniColors.error else OmniColors.textSecondary)
        MetricDivider()
        MetricCell("DURATION", formatDuration(metrics.durationMs), OmniColors.secondary)
        MetricDivider()
        MetricCell("TRANSPORT", metrics.transportState.name.take(4), metrics.transportState.toColor())
        MetricDivider()
        MetricCell("WARN", "${metrics.warningCount}", if (metrics.warningCount > 0) OmniColors.warning else OmniColors.textSecondary)
    }
}

@Composable
private fun MetricCell(label: String, value: String, valueColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = OmniColors.textSecondary, fontFamily = FontFamily.Monospace, fontSize = 9.sp, letterSpacing = 1.sp)
        Text(value, color = valueColor, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}

@Composable
private fun MetricDivider() { Box(modifier = Modifier.width(1.dp).height(28.dp).background(OmniColors.border)) }

@Composable
fun ExecutionProgressBar(progress: Float, state: ExecutionState) {
    val animatedProgress by animateFloatAsState(targetValue = progress, animationSpec = tween(300), label = "progress")
    val progressColor = state.toColor()
    Box(modifier = Modifier.fillMaxWidth().height(3.dp).background(OmniColors.border)) {
        Box(modifier = Modifier.fillMaxWidth(animatedProgress).fillMaxHeight().background(Brush.horizontalGradient(listOf(progressColor.copy(alpha = 0.6f), progressColor))))
    }
}

@Composable
private fun LiveTerminal(events: List<LiveTerminalEntry>, listState: LazyListState, onScrollChanged: (Boolean) -> Unit) {
    val isAtBottom by remember { derivedStateOf { val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index; val total = listState.layoutInfo.totalItemsCount; lastVisible != null && lastVisible >= total - 2 } }
    LaunchedEffect(isAtBottom) { onScrollChanged(isAtBottom) }
    LazyColumn(state = listState, modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp), contentPadding = PaddingValues(vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(1.dp)) {
        items(items = events, key = { "${it.timestamp}_${it.message.hashCode()}" }) { entry -> TerminalLogEntry(entry = entry) }
    }
}

@Composable
private fun TerminalLogEntry(entry: LiveTerminalEntry) {
    val levelColor = entry.level.toLogLevelColor()
    Row(modifier = Modifier.fillMaxWidth().then(if (entry.isHighlight) Modifier.background(levelColor.copy(alpha = 0.05f)) else Modifier).padding(horizontal = 12.dp, vertical = 2.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
        Text(formatTimestamp(entry.timestamp), color = OmniColors.textSecondary.copy(alpha = 0.5f), fontFamily = FontFamily.Monospace, fontSize = 10.sp, modifier = Modifier.width(52.dp))
        Text(entry.level, color = levelColor, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 10.sp, modifier = Modifier.width(36.dp))
        Box(modifier = Modifier.width(2.dp).height(14.dp).background(entry.category.toColor().copy(alpha = 0.6f)))
        Text(entry.message, color = if (entry.isHighlight) OmniColors.textPrimary else OmniColors.textPrimary.copy(alpha = 0.8f), fontFamily = FontFamily.Monospace, fontSize = 11.sp, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun StateIndicator(state: ExecutionState) {
    val color = state.toColor()
    val isPulsing = state == ExecutionState.RUNNING
    val alpha by if (isPulsing) {
        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
        infiniteTransition.animateFloat(initialValue = 0.4f, targetValue = 1f, animationSpec = infiniteRepeatable(animation = tween(800, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse), label = "alpha")
    } else { remember { mutableFloatStateOf(1f) } }

    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.clip(RoundedCornerShape(3.dp)).border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(3.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
        Box(modifier = Modifier.size(6.dp).clip(RoundedCornerShape(50)).background(color.copy(alpha = alpha)))
        Text(state.name, color = color, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 10.sp, letterSpacing = 1.sp)
    }
}

@Composable
private fun TerminalBottomBar(session: PayloadSession?, onClear: () -> Unit, onCancel: () -> Unit) {
    HorizontalDivider(color = OmniColors.border)
    Row(modifier = Modifier.fillMaxWidth().background(OmniColors.surface).navigationBarsPadding().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text("SID:${session?.sessionId?.take(8) ?: "--------"}", color = OmniColors.textSecondary, fontFamily = FontFamily.Monospace, fontSize = 10.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(modifier = Modifier.clip(RoundedCornerShape(3.dp)).clickable(onClick = onClear).padding(horizontal = 8.dp, vertical = 4.dp)) { Text("CLEAR", color = OmniColors.textSecondary, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 10.sp, letterSpacing = 1.sp) }
            if (session?.isTerminal == false) {
                Box(modifier = Modifier.clip(RoundedCornerShape(3.dp)).background(OmniColors.error.copy(alpha = 0.2f)).border(1.dp, OmniColors.error.copy(alpha = 0.8f), RoundedCornerShape(3.dp)).clickable(onClick = onCancel).padding(horizontal = 8.dp, vertical = 4.dp)) {
                    Text("ABORT", color = OmniColors.error, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 10.sp, letterSpacing = 1.sp)
                }
            }
        }
    }
}

private fun formatDuration(ms: Long): String { val seconds = ms / 1000; val minutes = seconds / 60; return if (minutes > 0) "${minutes}m${seconds % 60}s" else "${seconds}s" }
private fun formatTimestamp(timestamp: Long): String { val s = (timestamp / 1000) % 86400; val h = s / 3600; val m = (s % 3600) / 60; val sec = s % 60; return "%02d:%02d:%02d".format(h, m, sec) }
private fun String.toLogLevelColor(): Color = when (this.trim()) { "CRIT" -> OmniColors.critical; "ERR " -> OmniColors.error; "WARN" -> OmniColors.warning; "INFO", "OK  " -> OmniColors.primary; else -> OmniColors.textSecondary }
private fun TerminalCategory.toColor(): Color = when (this) { TerminalCategory.SESSION -> OmniColors.primary; TerminalCategory.ACTION -> OmniColors.secondary; TerminalCategory.TRANSPORT -> OmniColors.warning; TerminalCategory.PARSE -> OmniColors.textSecondary; TerminalCategory.WARNING -> OmniColors.warning }
private fun TransportState.toColor(): Color = when (this) { TransportState.CONNECTED, TransportState.TRANSMITTING -> OmniColors.primary; TransportState.CONNECTING -> OmniColors.warning; TransportState.ERROR -> OmniColors.error; TransportState.DISCONNECTED -> OmniColors.textSecondary }
