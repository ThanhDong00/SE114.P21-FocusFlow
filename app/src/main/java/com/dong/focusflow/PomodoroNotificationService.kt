package com.dong.focusflow

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.dong.focusflow.data.local.entity.PomodoroSessionType

class PomodoroNotificationService : Service() {

    private lateinit var notificationManager: NotificationManager

    companion object {
        private const val ONGOING_CHANNEL_ID = "PomodoroTimerChannel"
        private const val COMPLETION_CHANNEL_ID = "PomodoroCompletionChannel"
        private const val ONGOING_NOTIFICATION_ID = 101
        private const val COMPLETION_NOTIFICATION_ID = 102

        const val EXTRA_REMAINING_MILLIS = "remainingMillis"
        const val EXTRA_SESSION_TYPE = "sessionType"
        const val EXTRA_IS_RUNNING = "isRunning"
        const val ACTION_RESTORE_TIMER = "com.dong.focusflow.ACTION_RESTORE_TIMER"
        const val ACTION_SESSION_FINISHED = "com.dong.focusflow.ACTION_SESSION_FINISHED"
        const val EXTRA_FINISHED_SESSION_TYPE = "finishedSessionType"
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannels()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_SESSION_FINISHED -> {
                val finishedSessionType = intent.getStringExtra(EXTRA_FINISHED_SESSION_TYPE)?.let {
                    PomodoroSessionType.valueOf(it)
                } ?: PomodoroSessionType.FOCUS
                showSessionCompletionNotification(finishedSessionType)
                stopSelf()
            }
            else -> {
                val remainingMillis = intent?.getLongExtra(EXTRA_REMAINING_MILLIS, 0L) ?: 0L
                val sessionType = intent?.getStringExtra(EXTRA_SESSION_TYPE)?.let {
                    PomodoroSessionType.valueOf(it)
                } ?: PomodoroSessionType.FOCUS
                val isRunning = intent?.getBooleanExtra(EXTRA_IS_RUNNING, false) ?: false
                updateOngoingNotification(remainingMillis, sessionType, isRunning)
            }
        }
        return START_STICKY
    }

    /**
     * Creates notification channels for Android Oreo (API 26) and above.
     */
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ongoingChannel = NotificationChannel(
                ONGOING_CHANNEL_ID,
                "Pomodoro Timer Channel",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows ongoing Pomodoro timer notifications."
                setSound(null, null)
                enableVibration(false)
            }

            val completionChannel = NotificationChannel(
                COMPLETION_CHANNEL_ID,
                "Pomodoro Completion",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications when a Pomodoro session or break ends."
                enableLights(true)
                lightColor = getColor(R.color.purple_200)
                enableVibration(true)
                setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), null)
            }

            notificationManager.createNotificationChannel(ongoingChannel)
            notificationManager.createNotificationChannel(completionChannel)
        }
    }

    /**
     * Updates the ongoing notification with the current timer state.
     */
    private fun updateOngoingNotification(remainingMillis: Long, sessionType: PomodoroSessionType, isRunning: Boolean) {
        val minutes = (remainingMillis / 1000 / 60).toInt()
        val seconds = (remainingMillis / 1000 % 60).toInt()
        val formattedTime = String.format("%02d:%02d", minutes, seconds)

        val sessionTitle = when (sessionType) {
            PomodoroSessionType.FOCUS -> "Focus"
            PomodoroSessionType.SHORT_BREAK -> "Short Break"
            PomodoroSessionType.LONG_BREAK -> "Long Break"
        }

        val notificationText = if (isRunning) {
            "Time remaining: $formattedTime"
        } else {
            "Paused ($formattedTime)"
        }

        val notificationIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            action = ACTION_RESTORE_TIMER
            putExtra(EXTRA_REMAINING_MILLIS, remainingMillis)
            putExtra(EXTRA_SESSION_TYPE, sessionType.name)
            putExtra(EXTRA_IS_RUNNING, isRunning)
        }
        
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, ONGOING_CHANNEL_ID)
            .setContentTitle("Pomodoro: $sessionTitle")
            .setContentText(notificationText)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        startForeground(ONGOING_NOTIFICATION_ID, notification)
    }

    /**
     * Displays a notification when a Pomodoro session or break completes.
     */
    private fun showSessionCompletionNotification(finishedSessionType: PomodoroSessionType) {
        val title = when (finishedSessionType) {
            PomodoroSessionType.FOCUS -> "Focus session completed!"
            PomodoroSessionType.SHORT_BREAK -> "Short break ended!"
            PomodoroSessionType.LONG_BREAK -> "Long break ended!"
        }
        val message = "You can start the next session."

        val notificationIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            action = ACTION_RESTORE_TIMER
        }
        
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val completionNotification = NotificationCompat.Builder(this, COMPLETION_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        notificationManager.notify(COMPLETION_NOTIFICATION_ID, completionNotification)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(true)
        notificationManager.cancel(COMPLETION_NOTIFICATION_ID)
    }
}