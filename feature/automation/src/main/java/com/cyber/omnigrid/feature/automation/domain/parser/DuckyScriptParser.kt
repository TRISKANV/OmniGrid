package com.cyber.omnigrid.feature.automation.domain.parser

import com.cyber.omnigrid.feature.automation.domain.model.DuckyAction

class DuckyScriptParser {

    /**
     * Convierte el texto bruto en una lista secuencial de acciones.
     */
    fun parse(script: String): List<DuckyAction> {
        val actions = mutableListOf<DuckyAction>()
        val lines = script.lines()

        for (line in lines) {
            val trimmed = line.trim()
            
            // Ignorar líneas vacías o comentarios (REM)
            if (trimmed.isEmpty() || trimmed.startsWith("REM ") || trimmed == "REM") {
                continue
            }

            when {
                trimmed.startsWith("STRING ") -> {
                    // Extraer todo lo que está después de "STRING " respetando los espacios
                    val text = trimmed.substringAfter("STRING ")
                    actions.add(DuckyAction.TypeString(text))
                }
                trimmed.startsWith("DELAY ") -> {
                    val time = trimmed.substringAfter("DELAY ").trim().toLongOrNull() ?: 0L
                    actions.add(DuckyAction.Delay(time))
                }
                trimmed.startsWith("DEFAULT_DELAY ") || trimmed.startsWith("DEFAULTDELAY ") -> {
                    val time = trimmed.substringAfter("DELAY ").trim().toLongOrNull() ?: 0L
                    actions.add(DuckyAction.DefaultDelay(time))
                }
                else -> {
                    // Si no es STRING, ni DELAY, asumimos que es un comando de teclas (ej: "GUI r", "ENTER")
                    val keys = trimmed.split("\\s+".toRegex())
                    actions.add(DuckyAction.PressKey(keys))
                }
            }
        }
        return actions
    }
}
