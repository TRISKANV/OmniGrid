package com.tuapp.calculadora.ui.system

/**
 * MOCK TEMPORAL: Este objeto sustituye a la antigua capa de sesión
 * para mantener la compatibilidad de ModularDashboard.kt tras la refactorización.
 */
object SessionOrchestrator {
    val isSessionActive: Boolean = true
    val currentToken: String = "OMNI_SYS_TOKEN_MOCK"
    
    // Variables agregadas para destrabar la línea 118
    val sessionId: String = "SESSION_12345"
    val status: String = "ACTIVE"

    fun validateSession() = true
    fun clearSession() { /* No-op */ }
    
    fun bootstrapSession() { /* No-op */ }
    fun tick() { /* No-op */ }
    fun getSessionManifest(): String = "MOCK_MANIFEST_OK"
}
