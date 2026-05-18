package com.cyber.omnigrid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.cyber.omnigrid.core.database.OmniGridDatabase
import com.cyber.omnigrid.core.designsystem.theme.OmniGridTheme
import com.cyber.omnigrid.core.designsystem.theme.TrueBlack
import com.cyber.omnigrid.feature.automation.data.repository.OfflinePayloadRepository
import com.cyber.omnigrid.feature.automation.presentation.manager.PayloadViewModel
import com.cyber.omnigrid.navigation.OmniNavHost

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
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = TrueBlack
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
