package com.cyber.omnigrid.feature.automation.presentation.manager

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cyber.omnigrid.core.designsystem.components.CyberCard
import com.cyber.omnigrid.core.designsystem.theme.*
import com.cyber.omnigrid.feature.automation.domain.model.Payload

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayloadListScreen(
    viewModel: PayloadViewModel,
    onNavigateToEditor: (String?) -> Unit, 
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = TrueBlack,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToEditor(null) },
                containerColor = CyberAccent,
                contentColor = TrueBlack,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo Payload")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "// PAYLOAD_VAULT_MANAGER",
                color = CyberAccent,
                fontSize = 12.sp,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            // --- BARRA DE BÚSQUEDA Y FILTROS ---
            if (state is PayloadManagerUiState.Success || state is PayloadManagerUiState.Empty) {
                val currentQuery = (state as? PayloadManagerUiState.Success)?.searchQuery ?: ""
                val isFavActive = (state as? PayloadManagerUiState.Success)?.isFilterFavoritesActive ?: false

                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = currentQuery,
                        onValueChange = { viewModel.onEvent(PayloadManagerUiEvent.UpdateSearchQuery(it)) },
                        modifier = Modifier.weight(1f).border(1.dp, BorderGray, RoundedCornerShape(8.dp)),
                        placeholder = { Text("Buscar script...", color = TextSecondary, fontSize = 14.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
                        colors = TextFieldDefaults.textFieldColors(
                            containerColor = DarkGrayCard,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            textColor = TextPrimary
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )

                    IconToggleButton(
                        checked = isFavActive,
                        onCheckedChange = { viewModel.onEvent(PayloadManagerUiEvent.ToggleFavoriteFilter(it)) },
                        modifier = Modifier.background(DarkGrayCard, RoundedCornerShape(8.dp)).border(1.dp, BorderGray, RoundedCornerShape(8.dp))
                    ) {
                        Icon(
                            imageVector = if (isFavActive) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Filtrar Favoritos",
                            tint = if (isFavActive) CyberAccent else TextSecondary
                        )
                    }
                }
            }

            // --- MANEJO DE ESTADOS REACTIVOS ---
            Crossfade(targetState = state, label = "state_transition") { currentState ->
                when (currentState) {
                    is PayloadManagerUiState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = CyberAccent, strokeWidth = 2.dp)
                        }
                    }
                    is PayloadManagerUiState.Empty -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("BÓVEDA VACÍA. CREA TU PRIMER PAYLOAD.", color = TextSecondary, fontSize = 12.sp)
                        }
                    }
                    is PayloadManagerUiState.Success -> {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(currentState.payloads, key = { it.id }) { payload ->
                                PayloadItemRow(
                                    payload = payload,
                                    onItemClick = { onNavigateToEditor(payload.id) },
                                    onFavoriteToggle = { 
                                        viewModel.onEvent(PayloadManagerUiEvent.TogglePayloadFavorite(payload.id, payload.isFavorite)) 
                                    }
                                )
                            }
                        }
                    }
                    is PayloadManagerUiState.Error -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("ERROR CRÍTICO: ${currentState.message}", color = Color.Red, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PayloadItemRow(
    payload: Payload,
    onItemClick: () -> Unit,
    onFavoriteToggle: () -> Unit
) {
    CyberCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onItemClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = payload.name, color = TextPrimary, fontSize = 15.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                Text(text = payload.description, color = TextSecondary, fontSize = 12.sp, maxLines = 1)
            }
            IconButton(onClick = onFavoriteToggle) {
                Icon(
                    imageVector = if (payload.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = null,
                    tint = if (payload.isFavorite) CyberAccent else BorderGray
                )
            }
        }
    }
}
