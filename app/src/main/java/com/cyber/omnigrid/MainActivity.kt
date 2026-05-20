package com.cyber.omnigrid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.cyber.omnigrid.core.database.OmniGridDatabase
import com.cyber.omnigrid.core.designsystem.theme.OmniGridTheme
import com.cyber.omnigrid.feature.automation.data.repository.OfflinePayloadRepository
import com.cyber.omnigrid.feature.automation.presentation.manager.PayloadViewModel
import com.cyber.omnigrid.navigation.OmniNavHost

// IMPORTANTE: Asegúrate de que esta importación coincida con la ruta real donde creaste el archivo.
// Basado en tus logs anteriores, debería ser esta:
import com.tuapp.calculadora.ui.dashboard.OmniOSCockpit

class MainActivity : ComponentActivity() {
    
    // Instanciación manual temporal (Próximo paso en escalabilidad: Hilt/Dagger)
    private lateinit var database: OmniGridDatabase
    private lateinit var payloadRepository: OfflinePayloadRepository
    private lateinit var payloadViewModel: PayloadViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Inicializar Room DB
        database = Room.databaseBuilder(
            applicationContext,
            OmniGridDatabase::class.java,
            "omnigrid_vault.db"
        ).build()
        
        // Inicializar Dominio y Presentación
        payloadRepository = OfflinePayloadRepository(database.payloadDao())
        payloadViewModel = PayloadViewModel(payloadRepository)

        setContent {
            OmniGridTheme {
                // 1. ENVOLVEMOS LA APLICACIÓN EN EL COCKPIT OPERATIVO
                // A partir de aquí, toda la app tiene acceso al LocalAdaptiveConfig (Estado de estrés, Blur, Animaciones).
                OmniOSCockpit {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        // 2. CAMBIO CRÍTICO: Transparente para dejar ver el "Ambient Pulse" del Cockpit de fondo.
                        // El Cockpit ya provee un fondo ultra oscuro (0xFF030303) por defecto.
                        color = Color.Transparent
                    ) {
                        val navController = rememberNavController()
                        
                        OmniNavHost(
                            navController = navController,
                            payloadViewModel = payloadViewModel
                        )
                    }
                }
            }
        }
    }
}
