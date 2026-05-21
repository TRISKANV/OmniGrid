package com.tuapp.calculadora.ui.system

/**
 * MOCK TEMPORAL: Este objeto sustituye a la antigua capa de sesión
 * para mantener la compatibilidad de ModularDashboard.kt tras la refactorización.
 */
object SessionOrchestrator {
    val isSessionActive: Boolean = true
    val currentToken: String = "OMNI_SYS_TOKEN_MOCK"

    fun validateSession() = true
    fun clearSession() { /* No-op */ }
}
