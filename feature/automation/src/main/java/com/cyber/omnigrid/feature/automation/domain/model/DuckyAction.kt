package com.cyber.omnigrid.feature.automation.domain.model

/**
 * Representa cualquier acción posible generada a partir de un payload de DuckyScript.
 * Esta es nuestra capa de abstracción. Ningún motor lee texto plano, todos leen esto.
 */
sealed class DuckyAction {
    // Escribir una cadena de texto (ej. STRING Hello World)
    data class TypeString(val text: String) : DuckyAction()
    
    // Presionar una o múltiples teclas (ej. GUI r, CTRL ALT DEL)
    data class PressKey(val keys: List<String>) : DuckyAction()
    
    // Pausa temporal en milisegundos (ej. DELAY 1000)
    data class Delay(val durationMs: Long) : DuckyAction()
    
    // Configura el delay por defecto entre comandos (ej. DEFAULTDELAY 500)
    data class DefaultDelay(val durationMs: Long) : DuckyAction()
}
