package com.cyber.omnigrid.navigation

/**
 * Definición centralizada de las rutas de navegación de la aplicación.
 * Soporta argumentos obligatorios y opcionales para Deep Linking.
 */
sealed class Screen(val route: String) {
    
    // Módulo Core
    object Dashboard : Screen("dashboard")
    
    // Módulo Automation (Rucky) - Atado al contexto del Workspace activo
    object PayloadList : Screen("payload_list/{workspaceId}") {
        fun createRoute(workspaceId: String) = "payload_list/$workspaceId"
    }
    
    // Argumento opcional (?payloadId=) define si es Creación (null) o Edición (String)
    object PayloadEditor : Screen("payload_editor/{workspaceId}?payloadId={payloadId}") {
        fun createRoute(workspaceId: String, payloadId: String? = null): String {
            return if (payloadId != null) "payload_editor/$workspaceId?payloadId=$payloadId"
            else "payload_editor/$workspaceId"
        }
    }
    
    object LiveExecution : Screen("live_execution/{payloadId}") {
        fun createRoute(payloadId: String) = "live_execution/$payloadId"
    }
}
