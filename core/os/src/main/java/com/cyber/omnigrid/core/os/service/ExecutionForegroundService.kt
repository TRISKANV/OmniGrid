package com.cyber.omnigrid.core.os.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancelableContinuation
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class ExecutionForegroundService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private lateinit var notificationManager: NotificationManager

    companion object {
        private const val CHANNEL_ID = "omnigrid_engine_runtime"
        private const val NOTIFICATION_ID = 0xDEADC0DE.toInt()

        // Comandos de Control mediante Intents externos o Notificaciones
        const val ACTION_START = "com.cyber.omnigrid.action.START"
        const val ACTION_PAUSE = "com.cyber.omnigrid.action.PAUSE"
        const val ACTION_RESUME = "com.cyber.omnigrid.action.RESUME"
        const val ACTION_STOP = "com.cyber.omnigrid.action.STOP"
    }

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
        observeSessionManager()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val taskName = intent.getStringExtra("EXTRA_TASK_NAME") ?: "CORE_RUN_DEFAULT"
                ExecutionSessionManager.startSession(taskName)
                startForeground(NOTIFICATION_ID, buildNotification(ExecutionSessionManager.sessionState.value))
            }
            ACTION_PAUSE -> ExecutionSessionManager.pauseSession()
            ACTION_RESUME -> ExecutionSessionManager.resumeSession()
            ACTION_STOP -> {
                ExecutionSessionManager.stopSession()
                stopSelf()
            }
        }
        
        // START_STICKY garantiza que si el sistema operativo mata el proceso por falta de RAM, 
        // recreará el servicio automáticamente tan pronto recupere recursos.
        return START_STICKY
    }

    private fun observeSessionManager() {
        ExecutionSessionManager.sessionState
            .onEach { state ->
                if (state.lifecycleState == EngineLifecycleState.TERMINATED) {
                    stopSelf()
                } else {
                    notificationManager.notify(NOTIFICATION_ID, buildNotification(state))
                }
            }
            .launchIn(serviceScope)
    }

    private fun buildNotification(state: SessionState): Notification {
        val percentage = (state.progress * 100).toInt()
        
        // Intent para reabrir la app al presionar la notificación
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingContentIntent = PendingIntent.getActivity(
            this, 0, launchIntent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Configuración de las Quick Actions Reactivas
        val actionIntent = if (state.lifecycleState == EngineLifecycleState.RUNNING) {
            Intent(this, ExecutionForegroundService::class.java).apply { action = ACTION_PAUSE }
        } else {
            Intent(this, ExecutionForegroundService::class.java).apply { action = ACTION_RESUME }
        }
        
        val actionLabel = if (state.lifecycleState == EngineLifecycleState.RUNNING) "PAUSE" else "RESUME"
        val pendingActionIntent = PendingIntent.getService(
            this, 1, actionIntent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, ExecutionForegroundService::class.java).apply { action = ACTION_STOP }
        val pendingStopIntent = PendingIntent.getService(
            this, 2, stopIntent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val subtextMetrics = "TX: ${state.metrics.txLatencyMs}ms | INJ: ${state.metrics.commandsInjected}"

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("OMNIGRID RUNTIME // ${state.currentTaskName}")
            .setContentText("Progreso: $percentage% | ${state.transportState}")
            .setSubText(subtextMetrics)
            // Nota: Aquí se mapeará el icono definitivo del design system en etapas posteriores
            .setSmallIcon(android.R.drawable.stat_notify_sync) 
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setContentIntent(pendingContentIntent)
            .setProgress(100, percentage, false)
            .addAction(android.R.drawable.ic_media_pause, actionLabel, pendingActionIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "TERMINATE", pendingStopIntent)
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle().setShowActionsInCompactView(0, 1))
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setPriority(NotificationCompat.PRIORITY_LOW) // Evita sonido molesto en cada actualización de telemetría
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "OmniGrid Engine Persistent Execution",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Mantiene los hilos síncronos y telemetría de inyección activos."
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        ExecutionSessionManager.stopSession()
    }
}
