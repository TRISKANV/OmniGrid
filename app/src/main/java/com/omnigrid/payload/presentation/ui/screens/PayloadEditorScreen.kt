package com.omnigrid.payload.presentation.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.omnigrid.payload.domain.model.PayloadCategory
import com.omnigrid.payload.presentation.viewmodel.PayloadEditorViewModel

@Composable
fun PayloadEditorScreen(
    viewModel: PayloadEditorViewModel,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var scriptField by remember { mutableStateOf(TextFieldValue(uiState.script)) }

    LaunchedEffect(uiState.script) {
        if (scriptField.text.isEmpty() && uiState.script.isNotEmpty()) {
            scriptField = TextFieldValue(uiState.script)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.saved.collect { onSaved() }
    }

    Box(modifier = Modifier.fillMaxSize().background(OmniColors.background)) {
        Column(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
            EditorHeader(isEditMode = uiState.isEditMode, isSaving = uiState.isSaving, onBack = onBack, onSave = { viewModel.save(scriptField.text) })

            Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OmniTextField(value = uiState.name, onValueChange = viewModel::setName, label = "PAYLOAD NAME", placeholder = "operation_nightfall", singleLine = true)
                OmniTextField(value = uiState.description, onValueChange = viewModel::setDescription, label = "DESCRIPTION", placeholder = "Optional mission brief...", singleLine = false, minLines = 2)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("CATEGORY", color = OmniColors.textSecondary, fontFamily = FontFamily.Monospace, fontSize = 10.sp, letterSpacing = 1.sp, modifier = Modifier.width(80.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                        PayloadCategory.values().forEach { cat ->
                            val catColor = Color(android.graphics.Color.parseColor(cat.colorHex))
                            val selected = uiState.category == cat
                            Box(
                                modifier = Modifier.clip(RoundedCornerShape(3.dp)).background(if (selected) catColor.copy(0.2f) else Color.Transparent)
                                    .border(1.dp, if (selected) catColor else OmniColors.border, RoundedCornerShape(3.dp)).clickable { viewModel.setCategory(cat) }.padding(horizontal = 10.dp, vertical = 5.dp)
                            ) { Text(cat.label.uppercase(), color = if (selected) catColor else OmniColors.textSecondary, fontFamily = FontFamily.Monospace, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal, fontSize = 10.sp) }
                        }
                    }
                }

                OmniTextField(value = uiState.tagsRaw, onValueChange = viewModel::setTagsRaw, label = "TAGS", placeholder = "recon, windows, admin  (comma separated)", singleLine = true)

                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("DUCKYSCRIPT", color = OmniColors.textSecondary, fontFamily = FontFamily.Monospace, fontSize = 10.sp, letterSpacing = 1.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            DuckySnippetButton("GUI r") { scriptField = scriptField.appendText("GUI r\n") }
                            DuckySnippetButton("STRING") { scriptField = scriptField.appendText("STRING ") }
                            DuckySnippetButton("DELAY") { scriptField = scriptField.appendText("DELAY 500\n") }
                            DuckySnippetButton("ENTER") { scriptField = scriptField.appendText("ENTER\n") }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))

                    Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp)).background(OmniColors.surface).border(1.dp, OmniColors.border, RoundedCornerShape(4.dp))) {
                        Row {
                            val lineCount = scriptField.text.lines().size
                            Column(modifier = Modifier.background(OmniColors.surfaceElevated).padding(horizontal = 8.dp, vertical = 12.dp), horizontalAlignment = Alignment.End) {
                                for (i in 1..maxOf(lineCount, 1)) {
                                    Text("$i", color = OmniColors.textSecondary.copy(0.4f), fontFamily = FontFamily.Monospace, fontSize = 12.sp, lineHeight = 20.sp)
                                }
                            }
                            BasicTextField(
                                value = scriptField, onValueChange = { scriptField = it },
                                modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 200.dp).padding(12.dp),
                                textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 13.sp, color = OmniColors.textPrimary, lineHeight = 20.sp),
                                decorationBox = { inner ->
                                    if (scriptField.text.isEmpty()) { Text("REM OmniGrid Payload\nDELAY 1000\nGUI r\nDELAY 500\nSTRING cmd\nENTER", color = OmniColors.textSecondary.copy(0.3f), fontFamily = FontFamily.Monospace, fontSize = 13.sp, lineHeight = 20.sp) }
                                    inner()
                                }
                            )
                        }
                    }
                    val currentLines = scriptField.text.lines().count { it.isNotBlank() }
                    Text("$currentLines lines", color = OmniColors.textSecondary.copy(0.5f), fontFamily = FontFamily.Monospace, fontSize = 10.sp, modifier = Modifier.fillMaxWidth().padding(top = 4.dp), textAlign = TextAlign.End)
                }

                uiState.error?.let { error ->
                    Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp)).background(OmniColors.error.copy(0.1f)).border(1.dp, OmniColors.error.copy(0.4f), RoundedCornerShape(4.dp)).padding(12.dp)) {
                        Text(error, color = OmniColors.error, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                    }
                }
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
private fun EditorHeader(isEditMode: Boolean, isSaving: Boolean, onBack: () -> Unit, onSave: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconButton(onClick = onBack, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.ArrowBack, "Back", tint = OmniColors.textSecondary, modifier = Modifier.size(18.dp)) }
            Text(if (isEditMode) "EDIT PAYLOAD" else "NEW PAYLOAD", color = OmniColors.primary, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 13.sp, letterSpacing = 2.sp)
        }
        Box(
            modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(if (isSaving) OmniColors.border else OmniColors.primary.copy(0.15f))
                .border(1.dp, if (isSaving) OmniColors.border else OmniColors.primary, RoundedCornerShape(4.dp)).clickable(enabled = !isSaving, onClick = onSave).padding(horizontal = 16.dp, vertical = 8.dp)
        ) { Text(if (isSaving) "SAVING..." else "SAVE", color = if (isSaving) OmniColors.textSecondary else OmniColors.primary, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 2.sp) }
    }
    HorizontalDivider(color = OmniColors.border)
}

@Composable
private fun OmniTextField(value: String, onValueChange: (String) -> Unit, label: String, placeholder: String, singleLine: Boolean, minLines: Int = 1) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, color = OmniColors.textSecondary, fontFamily = FontFamily.Monospace, fontSize = 10.sp, letterSpacing = 1.sp)
        OutlinedTextField(
            value = value, onValueChange = onValueChange, modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, color = OmniColors.textSecondary.copy(0.4f), fontFamily = FontFamily.Monospace, fontSize = 13.sp) },
            textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace, fontSize = 13.sp, color = OmniColors.textPrimary),
            singleLine = singleLine, minLines = minLines,
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OmniColors.primary, unfocusedBorderColor = OmniColors.border, cursorColor = OmniColors.primary, focusedContainerColor = OmniColors.surface, unfocusedContainerColor = OmniColors.surface)
        )
    }
}

@Composable
private fun DuckySnippetButton(label: String, onClick: () -> Unit) {
    Box(modifier = Modifier.clip(RoundedCornerShape(2.dp)).background(OmniColors.surfaceElevated).border(1.dp, OmniColors.border, RoundedCornerShape(2.dp)).clickable(onClick = onClick).padding(horizontal = 6.dp, vertical = 3.dp)) {
        Text(label, color = OmniColors.secondary, fontFamily = FontFamily.Monospace, fontSize = 10.sp)
    }
}

private fun TextFieldValue.appendText(text: String): TextFieldValue {
    val newText = this.text + text
    return copy(text = newText, selection = TextRange(newText.length))
}
