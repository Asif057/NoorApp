package com.noor.screen

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class TimerService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var timerJob: Job? = null
    private var currentSecondsLeft: Long = 1800L

    companion object {
        const val CHANNEL_ID = "noor_timer_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_START = "com.noor.screen.ACTION_START"
        const val ACTION_PAUSE = "com.noor.screen.ACTION_PAUSE"
        const val ACTION_RESET = "com.noor.screen.ACTION_RESET"
        const val EXTRA_DURATION_SECONDS = "extra_duration_seconds"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ACTION_START

        when (action) {
            ACTION_START -> {
                val duration = intent?.getLongExtra(EXTRA_DURATION_SECONDS, currentSecondsLeft) ?: currentSecondsLeft
                currentSecondsLeft = duration
                startForegroundWithNotification(formatTime(currentSecondsLeft))
                startCountdown()
            }
            ACTION_PAUSE -> {
                pauseCountdown()
                updateNotification("Timer Paused - ${formatTime(currentSecondsLeft)}")
            }
            ACTION_RESET -> {
                val duration = intent?.getLongExtra(EXTRA_DURATION_SECONDS, 1800L) ?: 1800L
                currentSecondsLeft = duration
                startForegroundWithNotification(formatTime(currentSecondsLeft))
                startCountdown()
            }
        }

        return START_STICKY
    }

    private fun startCountdown() {
        timerJob?.cancel()
        timerJob = serviceScope.launch {
            TimerManager.setTimerRunningState(true)
            while (isActive && currentSecondsLeft > 0) {
                delay(1000L)
                currentSecondsLeft--
                TimerManager.updateRemainingSeconds(currentSecondsLeft)

                if (currentSecondsLeft % 5 == 0L || currentSecondsLeft <= 10L) {
                    updateNotification("Remaining: ${formatTime(currentSecondsLeft)}")
                }

                if (currentSecondsLeft <= 0) {
                    TimerManager.setTimeUpState(true)
                    updateNotification("Time's Up! Social apps locked.")
                    break
                }
            }
        }
    }

    private fun pauseCountdown() {
        timerJob?.cancel()
        TimerManager.setTimerRunningState(false)
    }

    private fun startForegroundWithNotification(contentText: String) {
        val notification = buildNotification("Timer Active - $contentText")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun updateNotification(contentText: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, buildNotification(contentText))
    }

    private fun buildNotification(contentText: String): Notification {
        val pendingIntent: PendingIntent = Intent(this, MainActivity::class.java).let { notificationIntent ->
            PendingIntent.getActivity(
                this, 0, notificationIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Noor Screen Timer")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_gear_disguise)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Noor Screen Timer"
            val descriptionText = "Shows active countdown timer for screen time restrictions"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun formatTime(totalSeconds: Long): String {
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        timerJob?.cancel()
        super.onDestroy()
    }
}
