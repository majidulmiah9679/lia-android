package com.gemini.multiverse.core.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.gemini.multiverse.GeminiMultiverseApp
import com.gemini.multiverse.MainActivity
import com.gemini.multiverse.R
import com.gemini.multiverse.core.gemini.LiveSessionState
import com.gemini.multiverse.domain.personality.PersonalitiesCatalog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Foreground Service maintaining persistent full-duplex voice sessions
 * even when the user locks their device or switches to other apps.
 */
class VoiceSessionService : Service() {

    companion object {
        const val CHANNEL_ID = "lia_voice_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_START_SESSION = "com.gemini.multiverse.action.START_SESSION"
        const val ACTION_STOP_SESSION = "com.gemini.multiverse.action.STOP_SESSION"

        fun start(context: Context) {
            val intent = Intent(context, VoiceSessionService::class.java).apply {
                action = ACTION_START_SESSION
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, VoiceSessionService::class.java).apply {
                action = ACTION_STOP_SESSION
            }
            context.startService(intent)
        }
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var wakeLock: PowerManager.WakeLock? = null
    private var isSessionActive = false

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        acquireWakeLock()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP_SESSION -> {
                terminateSession()
                stopSelf()
            }
            ACTION_START_SESSION, null -> {
                startForegroundSession()
            }
        }
        return START_STICKY
    }

    private fun startForegroundSession() {
        if (isSessionActive) return
        isSessionActive = true

        val notification = buildNotification("Lia Voice is Active", "Listening & ready to speak...")
        startForeground(NOTIFICATION_ID, notification)

        val app = GeminiMultiverseApp.instance
        val settingsRepo = app.container.settingsRepository

        serviceScope.launch {
            val settings = settingsRepo.settingsState.value
            val persona = PersonalitiesCatalog.getById(settings.selectedPersonalityId)

            // Auto-reconnect loop
            var reconnectAttempts = 0
            while (isSessionActive) {
                val apiKey = settingsRepo.getApiKeySync()
                if (apiKey.isBlank()) {
                    delay(3000)
                    continue
                }

                // Monitor connection state and update ongoing notification
                // If disconnected unexpectedly, back off and reconnect
                delay(2000)
            }
        }
    }

    @SuppressLint("WakelockTimeout")
    private fun acquireWakeLock() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "Lia:VoiceSessionWakeLock"
        ).apply {
            acquire()
        }
    }

    private fun releaseWakeLock() {
        wakeLock?.let {
            if (it.isHeld) it.release()
        }
        wakeLock = null
    }

    private fun terminateSession() {
        isSessionActive = false
        releaseWakeLock()
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Lia Voice Live Session",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows live status and controls for active voice sessions"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(title: String, content: String): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val stopIntent = Intent(this, VoiceSessionService::class.java).apply {
            action = ACTION_STOP_SESSION
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "End Session", stopPendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        terminateSession()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
