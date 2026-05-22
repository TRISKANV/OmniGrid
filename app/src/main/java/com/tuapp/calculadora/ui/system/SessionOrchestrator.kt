package com.tuapp.calculadora.ui.system

// Le agregamos la variable "nombre" que faltaba en la línea 118
class MockSessionManifest {
    val sessionId: String = "SESSION_12345"
    val status: String = "ACTIVE"
    val nombre: String = "OmniUser" 
}

/**
 * MOCK TEMPORAL: Este objeto sustituye a la antigua capa de sesión
 * para mantener la compatibilidad de ModularDashboard.kt.
 */
object SessionOrchestrator {
    val isSessionActive: Boolean = true
    val currentToken: String = "OMNI_SYS_TOKEN_MOCK"
    
    // Las replicamos acá también por si acaso las busca sueltas
    val sessionId: String = "SESSION_12345"
    val status: String = "ACTIVE"
    val nombre: String = "OmniUser"

    fun validateSession() = true
    fun clearSession() { /* No-op */ }
    fun bootstrapSession() { /* No-op */ }
    fun tick() { /* No-op */ }
    
    fun getSessionManifest(): MockSessionManifest = MockSessionManifest()
}
