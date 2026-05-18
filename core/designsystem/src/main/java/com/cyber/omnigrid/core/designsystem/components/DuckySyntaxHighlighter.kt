package com.cyber.omnigrid.core.designsystem.components

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.font.FontWeight
import com.cyber.omnigrid.core.designsystem.theme.CyberAccent
import com.cyber.omnigrid.core.designsystem.theme.TextSecondary

/**
 * Intercepta el flujo de texto en Compose y resalta comandos DuckyScript sobre la marcha.
 */
class DuckySyntaxHighlighter : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val builder = AnnotatedString.Builder(text.text)
        
        val keywords = listOf("DELAY", "DEFAULTDELAY", "STRING", "ENTER", "GUI", "APP", "MENU", "SHIFT", "ALT", "CONTROL", "CTRL")
        val lines = text.text.split("\n")
        
        var currentOffset = 0
        for (line in lines) {
            val trimmedLine = line.trim()
            
            if (trimmedLine.startsWith("REM")) {
                // Pintar comentarios en gris/verde atenuado
                builder.addStyle(
                    style = SpanStyle(color = TextSecondary, fontWeight = FontWeight.Normal),
                    start = currentOffset,
                    end = currentOffset + line.length
                )
            } else {
                // Buscar comandos operativos al inicio de las palabras
                for (keyword in keywords) {
                    var index = line.indexOf(keyword)
                    while (index != -1) {
                        val startPos = currentOffset + index
                        val endPos = startPos + keyword.length
                        builder.addStyle(
                            style = SpanStyle(color = CyberAccent, fontWeight = FontWeight.Bold),
                            start = startPos,
                            end = endPos
                        )
                        index = line.indexOf(keyword, index + 1)
                    }
                }
            }
            currentOffset += line.length + 1 // Contabilizar el salto de línea
        }

        return TransformedText(builder.toAnnotatedString(), OffsetMapping.Identity)
    }
}
