package com.cyber.omnigrid.feature.automation.presentation.manager

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cyber.omnigrid.core.designsystem.components.CyberCard
import com.cyber.omnigrid.core.designsystem.components.DuckySyntaxHighlighter
import com.cyber.omnigrid.core.designsystem.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayloadEditorScreen(
    viewModel: PayloadViewModel,
    payloadId: String?, // null si es nuevo
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var scriptContent by remember { mutableStateOf("") }

    // Si estamos editando uno existente, precargamos sus datos una sola vez al iniciar
    LaunchedEffect(state) {
        if (payloadId != null && state is PayloadManagerUiState.Success) {
            val existing = (state as PayloadManagerUiState.Success).payloads.find { it.id == payloadId }
            existing?.let {
                name = it.name
                description = it.description
                scriptContent = it.content
            }
        }
    }

    val lineCount = scriptContent.split("\n").size

    Scaffold(
        containerColor = TrueBlack,
        topBar = {
            TopAppBar(
                title = { Text(if (payloadId == null) "// NEW_PAYLOAD" else "// EDIT_PAYLOAD", fontSize = 14.sp, fontFamily = FontFamily.Monospace) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = TrueBlack, titleContentColor = CyberAccent),
                actions = {
                    Button(
                        onClick = {
                            if (name.isNotEmpty() && scriptContent.isNotEmpty()) {
                                viewModel.onEvent(PayloadManagerUiEvent.CreatePayload(name, description, scriptContent))
                                onNavigateBack()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberAccent)
                    ) {
                        Text("SAVE TO VAULT", color = TrueBlack, fontSize = 11.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- CAMPOS DE METADATOS ---
            TextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth().border(1.dp, BorderGray, RoundedCornerShape(8.dp)),
                placeholder = { Text("Nombre del Payload", color = TextSecondary) },
                colors = TextFieldDefaults.textFieldColors(containerColor = DarkGrayCard, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent, textColor = TextPrimary),
                singleLine = true
            )

            TextField(
                value = description,
                onValueChange = { description = it },
                modifier = Modifier.fillMaxWidth().border(1.dp, BorderGray, RoundedCornerShape(8.dp)),
                placeholder = { Text("Descripción / Objetivo", color = TextSecondary) },
                colors = TextFieldDefaults.textFieldColors(containerColor = DarkGrayCard, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent, textColor = TextPrimary),
                singleLine = true
            )

            // --- EDITOR DE CÓDIGO PREMIUM ---
            Text(text = "SCRIPT TERMINAL (DUCKYSCRIPT)", color = TextSecondary, fontSize = 10.sp)
            
            CyberCard(modifier = Modifier.fillMaxWidth().weight(1f)) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // HUD Superior del Editor: Cuenta de líneas
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "MODE: DUCKY_LANG", color = CyberAccent, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        Text(text = "LINES: $lineCount", color = TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    }

                    // Campo de texto nativo de alta velocidad con resaltador visual inyectado
                    BasicTextField(
                        value = scriptContent,
                        onValueChange = { scriptContent = it },
                        modifier = Modifier.fillMaxSize().background(Color.Transparent),
                        textStyle = TextStyle(
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 20.sp
                        ),
                        cursorBrush = SolidColor(CyberAccent),
                        visualTransformation = remember { DuckySyntaxHighlighter() }
                    )
                }
            }
        }
    }
}
