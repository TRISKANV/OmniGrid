package com.tuapp.calculadora.ui.system

// Cambiamos "nombre" por "name" 
class MockSessionManifest {
    val sessionId: String = "SESSION_12345"
    val status: String = "ACTIVE"
    val name: String = "OmniUser" 
}

/**
 * MOCK TEMPORAL: Este objeto sustituye a la antigua capa de sesión
 * para mantener la compatibilidad de ModularDashboard.kt.
 */
object SessionOrchestrator {
    val isSessionActive: Boolean = true
    val currentToken: String = "OMNI_SYS_TOKEN_MOCK"
    
    val sessionId: String = "SESSION_12345"
    val status: String = "ACTIVE"
    val name: String = "OmniUser"

    fun validateSession() = true
    fun clearSession() { /* No-op */ }
    fun bootstrapSession() { /* No-op */ }
    fun tick() { /* No-op */ }
    
    fun getSessionManifest(): MockSessionManifest = MockSessionManifest()
}
