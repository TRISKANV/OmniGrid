package com.omnigrid.payload.runtime.transport

interface TransportLayer {
    suspend fun connect(): Boolean
    suspend fun disconnect()
    suspend fun sendKeyPress(key: String, modifiers: List<String>)
    suspend fun sendString(text: String)
    fun bytesSent(): Long
}

class SimulatedTransportLayer : TransportLayer {
    private var _bytesSent = 0L
    private var connected = false

    override suspend fun connect(): Boolean {
        kotlinx.coroutines.delay(50) // Simula latencia
        connected = true
        return true
    }

    override suspend fun disconnect() {
        kotlinx.coroutines.delay(20)
        connected = false
    }

    override suspend fun sendKeyPress(key: String, modifiers: List<String>) {
        if (!connected) throw IllegalStateException("Transport not connected")
        kotlinx.coroutines.delay(10) 
        val encoded = "$key:${modifiers.joinToString("+")}"
        _bytesSent += encoded.length.toLong()
    }

    override suspend fun sendString(text: String) {
        if (!connected) throw IllegalStateException("Transport not connected")
        for (char in text) { kotlinx.coroutines.delay(5) }
        _bytesSent += text.length.toLong()
    }

    override fun bytesSent(): Long = _bytesSent
}
