package com.omnigrid.payload.runtime.engine

sealed class DuckyAction {
    abstract val index: Int

    data class KeyPress(override val index: Int, val key: String, val modifiers: List<String> = emptyList()) : DuckyAction()
    data class StringType(override val index: Int, val text: String) : DuckyAction()
    data class StringTypeLine(override val index: Int, val text: String) : DuckyAction()
    data class Delay(override val index: Int, val millis: Long) : DuckyAction()
    data class DefaultDelay(override val index: Int, val millis: Long) : DuckyAction()
    data class Rem(override val index: Int, val comment: String) : DuckyAction()
    data class RepeatAction(override val index: Int, val count: Int) : DuckyAction()
    data class Led(override val index: Int, val state: Boolean) : DuckyAction()
    data class Unknown(override val index: Int, val raw: String) : DuckyAction()
}

data class ParseResult(
    val actions: List<DuckyAction>,
    val warnings: List<String>,
    val lineCount: Int,
    val actionCount: Int
)

class DuckyScriptParser {
    companion object {
        private val MODIFIER_KEYS = setOf("CTRL", "SHIFT", "ALT", "GUI", "WINDOWS", "COMMAND")
        private val SPECIAL_KEYS = setOf(
            "ENTER", "ESCAPE", "BACKSPACE", "TAB", "SPACE", "CAPSLOCK", "DELETE", "END", "HOME", 
            "INSERT", "NUMLOCK", "SCROLLLOCK", "PRINTSCREEN", "PAUSE", "BREAK", "UPARROW", 
            "DOWNARROW", "LEFTARROW", "RIGHTARROW", "F1", "F2", "F3", "F4", "F5", "F6", "F7", 
            "F8", "F9", "F10", "F11", "F12", "APP", "MENU", "POWER", "SLEEP"
        )
    }

    fun parse(script: String): ParseResult {
        val actions = mutableListOf<DuckyAction>()
        val warnings = mutableListOf<String>()
        var defaultDelayMs = 0L
        var lastAction: DuckyAction? = null
        var actionIndex = 0

        val lines = script.lines()

        for ((lineNumber, rawLine) in lines.withIndex()) {
            val line = rawLine.trim()
            if (line.isEmpty()) continue

            val parts = line.split(" ", limit = 2)
            val command = parts[0].uppercase()
            val argument = parts.getOrNull(1)?.trim() ?: ""

            val action: DuckyAction? = when (command) {
                "REM" -> DuckyAction.Rem(actionIndex, argument)
                "STRING" -> if (argument.isEmpty()) { warnings.add("Line ${lineNumber + 1}: STRING with no argument"); null } else DuckyAction.StringType(actionIndex, argument)
                "STRINGLN" -> if (argument.isEmpty()) { warnings.add("Line ${lineNumber + 1}: STRINGLN with no argument"); null } else DuckyAction.StringTypeLine(actionIndex, argument)
                "DELAY" -> {
                    val ms = argument.toLongOrNull()
                    if (ms == null) { warnings.add("Line ${lineNumber + 1}: Invalid DELAY value: $argument"); null } else DuckyAction.Delay(actionIndex, ms)
                }
                "DEFAULTDELAY", "DEFAULT_DELAY" -> {
                    val ms = argument.toLongOrNull()
                    if (ms == null) { warnings.add("Line ${lineNumber + 1}: Invalid DEFAULT_DELAY: $argument"); null } else { defaultDelayMs = ms; DuckyAction.DefaultDelay(actionIndex, ms) }
                }
                "REPEAT" -> {
                    val count = argument.toIntOrNull()
                    if (count == null || count <= 0) { warnings.add("Line ${lineNumber + 1}: Invalid REPEAT count"); null } 
                    else if (lastAction == null) { warnings.add("Line ${lineNumber + 1}: REPEAT with no previous action"); null } 
                    else {
                        repeat(count - 1) { actions.add(lastAction!!.copyWithIndex(++actionIndex)) }
                        DuckyAction.RepeatAction(actionIndex, count)
                    }
                }
                "LED_R", "LED_G", "LED_B" -> DuckyAction.Led(actionIndex, true)
                else -> parseKeyAction(actionIndex, command, argument, lineNumber, warnings)
            }

            action?.let {
                actions.add(it)
                if (it !is DuckyAction.Rem && it !is DuckyAction.DefaultDelay) lastAction = it
                if (defaultDelayMs > 0 && it !is DuckyAction.Delay && it !is DuckyAction.DefaultDelay && it !is DuckyAction.Rem) {
                    actions.add(DuckyAction.Delay(++actionIndex, defaultDelayMs))
                }
                actionIndex++
            }
        }

        return ParseResult(actions, warnings, lines.size, actions.count { it !is DuckyAction.Rem && it !is DuckyAction.DefaultDelay })
    }

    private fun parseKeyAction(index: Int, command: String, argument: String, lineNumber: Int, warnings: MutableList<String>): DuckyAction {
        val tokens = command.split("-")
        val modifiers = tokens.dropLast(1).filter { it in MODIFIER_KEYS }
        val key = tokens.last()

        return if (key in SPECIAL_KEYS || key.length == 1 || modifiers.isNotEmpty()) {
            val finalKey = if (argument.isNotEmpty() && key !in SPECIAL_KEYS) argument else key
            DuckyAction.KeyPress(index, finalKey, modifiers)
        } else {
            warnings.add("Line ${lineNumber + 1}: Unknown command: $command")
            DuckyAction.Unknown(index, "$command $argument".trim())
        }
    }

    private fun DuckyAction.copyWithIndex(newIndex: Int): DuckyAction = when (this) {
        is DuckyAction.KeyPress -> copy(index = newIndex)
        is DuckyAction.StringType -> copy(index = newIndex)
        is DuckyAction.StringTypeLine -> copy(index = newIndex)
        is DuckyAction.Delay -> copy(index = newIndex)
        else -> this
    }
}
