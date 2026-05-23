package com.omnigrid.payload.service

import android.app.*
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.omnigrid.payload.domain.model.ExecutionState
import kotlinx.coroutines.*

class PayloadForegroundService : Service() {

    private val sessionManager get() = PayloadServiceLocator.sessionManager!!
    private val repository get() = PayloadServiceLocator.repository!!
    private val engine get() = PayloadServiceLocator.engine!!

    private val serviceScope = CoroutineScope(
        SupervisorJob() + Dispatchers.IO + CoroutineName("PayloadService")
    )

    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        fun getService(): PayloadForegroundService = this@PayloadForegroundService
    }

    companion object {
        const val CHANNEL_ID = "omnigrid_payload_runtime"
        const val NOTIFICATION_ID = 1001
        const val ACTION_EXECUTE = "com.omnigrid.payload.EXECUTE"
        const val ACTION_CANCEL = "com.omnigrid.payload.CANCEL"
        const val ACTION_STOP = "com.omnigrid.payload.STOP"
        const val EXTRA_PAYLOAD_ID = "payload_id"
        const val EXTRA_SESSION_ID = "session_id"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildIdleNotification())
        observeSessionsForNotification()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_EXECUTE -> {
                val payloadId = intent.getStringExtra(EXTRA_PAYLOAD_ID) ?: return START_NOT_STICKY
                serviceScope.launch { sessionManager.enqueueExecution(payloadId) }
            }
            ACTION_CANCEL -> {
                val sessionId = intent.getStringExtra(EXTRA_SESSION_ID) ?: return START_NOT_STICKY
                sessionManager.cancel(sessionId)
            }
            ACTION_STOP -> {
                engine.cancelAll()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "OmniGrid Payload Runtime",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Shows active payload executions"
            setShowBadge(false)
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun buildIdleNotification(): Notification =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("OmniGrid Runtime")
            .setContentText("Ready")
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Usa tu ícono real acá
            .setOngoing(true)
            .setSilent(true)
            .build()

    private fun buildActiveNotification(payloadName: String, progress: Int, sessionId: String): Notification {
        val cancelIntent = Intent(this, PayloadForegroundService::class.java).apply {
            action = ACTION_CANCEL
            putExtra(EXTRA_SESSION_ID, sessionId)
        }
        val cancelPending = PendingIntent.getService(this, 0, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Executing: $payloadName")
            .setContentText("$progress% complete")
            .setProgress(100, progress, progress == 0)
            .setSmallIcon(android.R.drawable.ic_media_play) // Usa tu ícono real acá
            .setOngoing(true)
            .setSilent(true)
            .addAction(android.R.drawable.ic_delete, "Cancel", cancelPending)
            .build()
    }

    private fun observeSessionsForNotification() {
        serviceScope.launch {
            sessionManager.observeActiveSessions().collect { sessions ->
                val manager = getSystemService(NotificationManager::class.java)
                if (sessions.isEmpty()) {
                    manager.notify(NOTIFICATION_ID, buildIdleNotification())
                } else {
                    val primary = sessions.firstOrNull { it.state == ExecutionState.RUNNING } ?: sessions.first()
                    manager.notify(NOTIFICATION_ID, buildActiveNotification(primary.payloadName, (primary.progress * 100).toInt(), primary.sessionId))
                }
            }
        }
    }
}
