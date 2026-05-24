package com.omnigrid.payload.di

import android.content.Context
import com.omnigrid.payload.data.local.PayloadDatabase
import com.omnigrid.payload.data.repository.PayloadRepositoryImpl
import com.omnigrid.payload.domain.usecase.*
import com.omnigrid.payload.runtime.engine.DuckyRuntimeEngine
import com.omnigrid.payload.runtime.engine.DuckyScriptParser
import com.omnigrid.payload.runtime.events.*
import com.omnigrid.payload.runtime.session.RuntimeSessionManager
import com.omnigrid.payload.runtime.transport.SimulatedTransportLayer
import com.omnigrid.payload.service.PayloadServiceLocator
import com.tuapp.calculadora.core.plugin.PayloadRuntimePlugin // ⚠️ Ajustá este import a tu paquete real de plugins

object PayloadBootloader {
    
    fun boot(
        context: Context,
        coreEventBus: OmniRuntimeEventBus,
        telemetry: OmniTelemetryBridge,
        timeline: OmniTimelineBridge
    ): PayloadRuntimePlugin {
        
        // 1. Data Layer (Room & Repository)
        val db = PayloadDatabase.getDatabase(context)
        val repository = PayloadRepositoryImpl(db.payloadDao(), db.sessionDao())

        // 2. Core Engine & Transport
        val parser = DuckyScriptParser()
        val transport = SimulatedTransportLayer() // Acá luego inyectarás el UsbHidTransportLayer real
        val engine = DuckyRuntimeEngine(parser, transport)

        // 3. Event Dispatcher & Session Manager
        val dispatcher = PayloadEventDispatcher(coreEventBus, telemetry, timeline)
        val sessionManager = RuntimeSessionManager(engine, repository, dispatcher)

        // 4. Service Locator (Indispensable para el Foreground Service)
        PayloadServiceLocator.repository = repository
        PayloadServiceLocator.engine = engine
        PayloadServiceLocator.sessionManager = sessionManager

        // 5. Use Cases (Lógica de negocio pura)
        val getPayloads = GetPayloadsUseCase(repository)
        val createPayload = CreatePayloadUseCase(repository)
        val updatePayload = UpdatePayloadUseCase(repository)
        val deletePayload = DeletePayloadUseCase(repository)
        val executePayload = ExecutePayloadUseCase(repository)
        val manageSession = ManageSessionUseCase(repository)

        // 6. Retornamos el Plugin completamente ensamblado
        return PayloadRuntimePlugin(
            repository = repository,
            sessionManager = sessionManager,
            engine = engine,
            getPayloads = getPayloads,
            createPayload = createPayload,
            updatePayload = updatePayload,
            deletePayload = deletePayload,
            executePayload = executePayload,
            manageSession = manageSession
        )
    }
}
