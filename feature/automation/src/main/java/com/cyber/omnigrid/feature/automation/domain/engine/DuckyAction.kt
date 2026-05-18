package com.cyber.omnigrid.feature.automation.domain.engine

/**
 * Representa una acción atómica ya parseada.
 * Desacopla la sintaxis del DuckyScript del hardware real que la ejecutará.
 */
sealed interface DuckyAction {
    data class Delay(val timeMs: Long) : DuckyAction
    data class TypeString(val text: String) : DuckyAction
    data class KeyCombo(val keys: List<String>) : DuckyAction // Ej: ["GUI", "r"] o ["CTRL", "ALT", "DELETE"]
}
