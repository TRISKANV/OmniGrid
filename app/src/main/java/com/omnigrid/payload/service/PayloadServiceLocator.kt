package com.omnigrid.payload.service

import com.omnigrid.payload.domain.repository.PayloadRepository
import com.omnigrid.payload.runtime.engine.DuckyRuntimeEngine
import com.omnigrid.payload.runtime.session.RuntimeSessionManager

object PayloadServiceLocator {
    var sessionManager: RuntimeSessionManager? = null
    var repository: PayloadRepository? = null
    var engine: DuckyRuntimeEngine? = null
}
