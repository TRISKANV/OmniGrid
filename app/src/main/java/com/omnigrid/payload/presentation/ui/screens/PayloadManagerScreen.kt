package com.omnigrid.payload.presentation.ui.screens

import androidx.compose.animation.*
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.omnigrid.payload.domain.model.*
import com.omnigrid.payload.presentation.viewmodel.*
import kotlinx.coroutines.flow.collectLatest

object OmniColors {
    val background = Color(0xFF000000)
    val surface = Color(0xFF0A0A0F)
    val surfaceElevated = Color(0xFF12121A)
    val border = Color(0xFF1E1E2E)
    val borderActive = Color(0xFF00FF88)
    val primary = Color(0xFF00FF88)
    val secondary = Color(0xFF0088FF)
    val warning = Color(0xFFFFAA00)
    val error = Color(0xFFFF3355)
    val critical = Color(0xFFFF0033)
    val textPrimary = Color(0xFFE8E8FF)
    val textSecondary = Color(0xFF7777AA)
    val textMono = Color(0xFF00FF88)
    val glassBg = Color(0x1A00FF88)
}

@Composable
fun PayloadManagerScreen(
    viewModel: PayloadManagerViewModel,
    onNavigateToTerminal: (String) -> Unit,
    onNavigateToEditor: (String?) -> Unit
) {
    val payloads by viewModel.payloads.collectAsState()
    val activeSessions by viewModel.activeSessions.collectAsState()
    val stats by viewModel.dashboardStats.collectAsState()
    val filterState by viewModel.filterState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is ManagerUiEvent.ExecutionStarted -> onNavigateToTerminal(event.sessionId)
                else -> {}
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(OmniColors.background)) {
        Column(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
            PayloadManagerHeader(stats = stats, onCreateNew = { onNavigateToEditor(null) })

            AnimatedVisibility(visible = activeSessions.isNotEmpty()) {
                ActiveSessionsBanner(
                    sessions = activeSessions,
                    onSessionClick = { onNavigateToTerminal(it.sessionId) },
                    onCancel = { viewModel.cancelSession(it) }
                )
            }

            FilterBar(
                filterState = filterState,
                onQueryChange = viewModel::setQuery,
                onCategorySelect = viewModel::setCategory,
                onFavoritesToggle = { viewModel.setFavoritesOnly(!filterState.onlyFavorites) }
            )

            PayloadList(
                payloads = payloads,
                activeSessions = activeSessions,
                onExecute = { viewModel.executePayload(it.id) },
                onEdit = { onNavigateToEditor(it.id) },
                onDelete = { viewModel.deletePayload(it.id) },
                onFavoriteToggle = { viewModel.toggleFavorite(it.id) }
            )
        }
    }
}

@Composable
private fun PayloadManagerHeader(stats: DashboardStats, onCreateNew: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("PAYLOAD MANAGER", color = OmniColors.primary, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 14.sp, letterSpacing = 3.sp)
            Text("${stats.totalExecutions} executions  ·  ${(stats.successRate * 100).toInt()}% success", color = OmniColors.textSecondary, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (stats.activeSessions > 0) ActivePulse(count = stats.activeSessions)
            Box(
                modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(OmniColors.glassBg).border(1.dp, OmniColors.primary, RoundedCornerShape(4.dp))
                    .clickable(onClick = onCreateNew).padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text("+ NEW", color = OmniColors.primary, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 2.sp)
            }
        }
    }
    HorizontalDivider(color = OmniColors.border, thickness = 1.dp)
}

@Composable
private fun ActiveSessionsBanner(sessions: List<PayloadSession>, onSessionClick: (PayloadSession) -> Unit, onCancel: (String) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().background(Brush.horizontalGradient(listOf(Color(0x2200FF88), Color(0x0A00FF88))))
            .border(1.dp, OmniColors.primary.copy(alpha = 0.3f)).padding(12.dp)
    ) {
        Text("ACTIVE EXECUTIONS", color = OmniColors.primary, fontFamily = FontFamily.Monospace, fontSize = 10.sp, letterSpacing = 2.sp)
        Spacer(modifier = Modifier.height(8.dp))
        sessions.forEach { session ->
            ActiveSessionRow(session = session, onClick = { onSessionClick(session) }, onCancel = { onCancel(session.sessionId) })
        }
    }
}

@Composable
private fun ActiveSessionRow(session: PayloadSession, onClick: () -> Unit, onCancel: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(session.payloadName, color = OmniColors.textPrimary, fontFamily = FontFamily.Monospace, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            LinearProgressIndicator(progress = { session.progress }, modifier = Modifier.fillMaxWidth().height(2.dp).padding(top = 4.dp), color = OmniColors.primary, trackColor = OmniColors.border)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(session.state.name, color = session.state.toColor(), fontFamily = FontFamily.Monospace, fontSize = 10.sp)
        IconButton(onClick = onCancel, modifier = Modifier.size(24.dp)) { Icon(Icons.Default.Close, contentDescription = "Cancel", tint = OmniColors.error, modifier = Modifier.size(14.dp)) }
    }
}

@Composable
private fun FilterBar(filterState: FilterState, onQueryChange: (String) -> Unit, onCategorySelect: (PayloadCategory?) -> Unit, onFavoritesToggle: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        OutlinedTextField(
            value = filterState.query, onValueChange = onQueryChange, modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("search payloads...", color = OmniColors.textSecondary, fontFamily = FontFamily.Monospace, fontSize = 13.sp) },
            textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace, fontSize = 13.sp, color = OmniColors.textPrimary),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OmniColors.primary, unfocusedBorderColor = OmniColors.border, cursorColor = OmniColors.primary, focusedContainerColor = OmniColors.surface, unfocusedContainerColor = OmniColors.surface),
            singleLine = true, leadingIcon = { Icon(Icons.Default.Search, null, tint = OmniColors.textSecondary, modifier = Modifier.size(16.dp)) }
        )
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            item { CategoryChip("ALL", filterState.category == null && !filterState.onlyFavorites, onClick = { onCategorySelect(null) }) }
            item { CategoryChip("★ FAV", filterState.onlyFavorites, OmniColors.warning, onClick = onFavoritesToggle) }
            items(PayloadCategory.values()) { category ->
                CategoryChip(category.label.uppercase(), filterState.category == category, Color(android.graphics.Color.parseColor(category.colorHex)), onClick = { onCategorySelect(if (filterState.category == category) null else category) })
            }
        }
    }
}

@Composable
private fun CategoryChip(label: String, selected: Boolean, color: Color = OmniColors.primary, onClick: () -> Unit) {
    Box(
        modifier = Modifier.clip(RoundedCornerShape(3.dp)).background(if (selected) color.copy(alpha = 0.2f) else Color.Transparent)
            .border(1.dp, if (selected) color else OmniColors.border, RoundedCornerShape(3.dp)).clickable(onClick = onClick).padding(horizontal = 10.dp, vertical = 4.dp)
    ) { Text(label, color = if (selected) color else OmniColors.textSecondary, fontFamily = FontFamily.Monospace, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal, fontSize = 10.sp, letterSpacing = 1.sp) }
}

@Composable
private fun PayloadList(payloads: List<Payload>, activeSessions: List<PayloadSession>, onExecute: (Payload) -> Unit, onEdit: (Payload) -> Unit, onDelete: (Payload) -> Unit, onFavoriteToggle: (Payload) -> Unit) {
    if (payloads.isEmpty()) { EmptyState(); return }
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(items = payloads, key = { it.id }) { payload ->
            val isRunning = activeSessions.any { it.payloadId == payload.id }
            PayloadCard(payload, isRunning, { onExecute(payload) }, { onEdit(payload) }, { onDelete(payload) }, { onFavoriteToggle(payload) })
        }
        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
fun PayloadCard(payload: Payload, isRunning: Boolean, onExecute: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit, onFavoriteToggle: () -> Unit, modifier: Modifier = Modifier) {
    val categoryColor = Color(android.graphics.Color.parseColor(payload.category.colorHex))
    val borderColor = if (isRunning) OmniColors.primary else OmniColors.border
    Box(modifier = modifier.fillMaxWidth().clip(RoundedCornerShape(6.dp)).background(OmniColors.surfaceElevated).border(if (isRunning) 1.5.dp else 1.dp, borderColor, RoundedCornerShape(6.dp))) {
        Box(modifier = Modifier.width(3.dp).fillMaxHeight().background(categoryColor).align(Alignment.CenterStart))
        Column(modifier = Modifier.fillMaxWidth().padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 10.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(payload.name, color = OmniColors.textPrimary, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    if (payload.description.isNotBlank()) Text(payload.description, color = OmniColors.textSecondary, fontFamily = FontFamily.Monospace, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                IconButton(onClick = onFavoriteToggle, modifier = Modifier.size(24.dp)) { Icon(if (payload.isFavorite) Icons.Default.Star else Icons.Default.StarBorder, "Favorite", tint = if (payload.isFavorite) OmniColors.warning else OmniColors.textSecondary, modifier = Modifier.size(16.dp)) }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.clip(RoundedCornerShape(2.dp)).background(categoryColor.copy(alpha = 0.15f)).border(1.dp, categoryColor.copy(alpha = 0.4f), RoundedCornerShape(2.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                    Text(payload.category.label.uppercase(), color = categoryColor, fontFamily = FontFamily.Monospace, fontSize = 9.sp, letterSpacing = 1.sp)
                }
                payload.tags.take(3).forEach { Text("#$it", color = OmniColors.secondary.copy(alpha = 0.7f), fontFamily = FontFamily.Monospace, fontSize = 10.sp) }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(payload.script.lines().take(2).joinToString(" ↵ "), color = OmniColors.textSecondary.copy(alpha = 0.6f), fontFamily = FontFamily.Monospace, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("× ${payload.executionCount}", color = OmniColors.textSecondary, fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CompactActionButton("EDIT", OmniColors.secondary, onEdit)
                    CompactActionButton("DEL", OmniColors.error, onDelete)
                    CompactActionButton(if (isRunning) "RUNNING" else "EXEC", if (isRunning) OmniColors.warning else OmniColors.primary, if (isRunning) {} else onExecute, true)
                }
            }
        }
        if (isRunning) Box(modifier = Modifier.matchParentSize().background(Brush.horizontalGradient(listOf(Color.Transparent, OmniColors.primary.copy(alpha = 0.04f)))))
    }
}

@Composable
private fun CompactActionButton(label: String, color: Color, onClick: () -> Unit, filled: Boolean = false) {
    Box(modifier = Modifier.clip(RoundedCornerShape(3.dp)).background(if (filled) color.copy(alpha = 0.2f) else Color.Transparent).border(1.dp, color.copy(alpha = if (filled) 0.8f else 0.4f), RoundedCornerShape(3.dp)).clickable(onClick = onClick).padding(horizontal = 8.dp, vertical = 4.dp)) {
        Text(label, color = color, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 10.sp, letterSpacing = 1.sp)
    }
}

@Composable
private fun ActivePulse(count: Int) {
    Box(modifier = Modifier.clip(RoundedCornerShape(3.dp)).background(OmniColors.primary.copy(alpha = 0.15f)).border(1.dp, OmniColors.primary, RoundedCornerShape(3.dp)).padding(horizontal = 8.dp, vertical = 4.dp), contentAlignment = Alignment.Center) {
        Text("● $count ACTIVE", color = OmniColors.primary, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 9.sp, letterSpacing = 1.sp)
    }
}

@Composable
private fun EmptyState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("[ NO PAYLOADS ]", color = OmniColors.textSecondary, fontFamily = FontFamily.Monospace, fontSize = 14.sp, letterSpacing = 2.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text("create your first payload to begin", color = OmniColors.textSecondary.copy(alpha = 0.5f), fontFamily = FontFamily.Monospace, fontSize = 11.sp)
        }
    }
}

fun ExecutionState.toColor(): Color = when (this) {
    ExecutionState.QUEUED -> OmniColors.textSecondary
    ExecutionState.INITIALIZING -> OmniColors.secondary
    ExecutionState.RUNNING -> OmniColors.primary
    ExecutionState.PAUSED -> OmniColors.warning
    ExecutionState.COMPLETING -> OmniColors.primary
    ExecutionState.COMPLETED -> OmniColors.primary
    ExecutionState.FAILED -> OmniColors.error
    ExecutionState.CANCELLED -> OmniColors.textSecondary
    ExecutionState.TIMEOUT -> OmniColors.critical
}
