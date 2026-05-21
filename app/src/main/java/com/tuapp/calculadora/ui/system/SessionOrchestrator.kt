package com.tuapp.calculadora.ui.system

// 1. Creamos el molde falso para el manifiesto que busca la línea 118
class MockSessionManifest {
    val sessionId: String = "SESSION_12345"
    val status: String = "ACTIVE"
}

/**
 * MOCK TEMPORAL: Este objeto sustituye a la antigua capa de sesión
 * para mantener la compatibilidad de ModularDashboard.kt.
 */
object SessionOrchestrator {
    val isSessionActive: Boolean = true
    val currentToken: String = "OMNI_SYS_TOKEN_MOCK"
    
    // Por si acaso también las buscaba directo desde el Orchestrator
    val sessionId: String = "SESSION_12345"
    val status: String = "ACTIVE"

    fun validateSession() = true
    fun clearSession() { /* No-op */ }
    fun bootstrapSession() { /* No-op */ }
    fun tick() { /* No-op */ }
    
    // 2. ACÁ ESTABA EL ERROR: Ahora devolvemos el objeto con las variables, no un String.
    fun getSessionManifest(): MockSessionManifest = MockSessionManifest()
}
