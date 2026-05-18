package com.cyber.omnigrid.feature.automation.domain.engine

/**
 * Transforma el texto crudo en una lista estructurada de acciones.
 */
class DuckyScriptParser {

    fun parse(script: String): List<DuckyAction> {
        val actions = mutableListOf<DuckyAction>()
        val lines = script.lines().map { it.trim() }.filter { it.isNotEmpty() }

        var defaultDelay = 0L

        for ((index, line) in lines.withIndex()) {
            if (line.startsWith("REM")) continue // Ignorar comentarios

            try {
                when {
                    line.startsWith("DEFAULTDELAY") || line.startsWith("DEFAULT_DELAY") -> {
                        defaultDelay = line.split(" ")[1].toLong()
                    }
                    line.startsWith("DELAY") -> {
                        actions.add(DuckyAction.Delay(line.split(" ")[1].toLong()))
                    }
                    line.startsWith("STRING") -> {
                        val text = line.substringAfter("STRING ")
                        actions.add(DuckyAction.TypeString(text))
                    }
                    else -> {
                        // Si no es ninguno de los anteriores, asumimos que es un KeyCombo (Ej: GUI r, ENTER)
                        val keys = line.split(" ")
                        actions.add(DuckyAction.KeyCombo(keys))
                    }
                }

                // Inyectar el Default Delay si está configurado
                if (defaultDelay > 0 && !line.startsWith("DEFAULTDELAY")) {
                    actions.add(DuckyAction.Delay(defaultDelay))
                }
            } catch (e: Exception) {
                throw IllegalArgumentException("Error de sintaxis en la línea ${index + 1}: '$line'")
            }
        }
        return actions
    }
}
