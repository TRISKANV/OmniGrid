package com.tuapp.calculadora.ui.system

// 1. Creamos un Enum falso para engañar a la llamada "status.name"
enum class MockSessionStatus {
    ACTIVE
}

class MockSessionManifest {
    val sessionId: String = "SESSION_12345"
    // 2. Ahora status es un Enum. ¡Al hacer status.name devolverá "ACTIVE"!
    val status: MockSessionStatus = MockSessionStatus.ACTIVE
    
    // Lo dejamos por si las moscas, no molesta
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
    val status: MockSessionStatus = MockSessionStatus.ACTIVE
    val name: String = "OmniUser"

    fun validateSession() = true
    fun clearSession() { /* No-op */ }
    fun bootstrapSession() { /* No-op */ }
    fun tick() { /* No-op */ }
    
    fun getSessionManifest(): MockSessionManifest = MockSessionManifest()
}
