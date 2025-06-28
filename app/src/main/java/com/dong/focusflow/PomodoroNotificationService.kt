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

class PomodoroNotificationService: Service() {
    private val ONGOING_CHANNEL_ID = "PomodoroTimerChannel"
    private val COMPLETION_CHANNEL_ID = "PomodoroCompletionChannel" // New channel for completion notifications
    private val ONGOING_NOTIFICATION_ID = 101
    private val COMPLETION_NOTIFICATION_ID = 102 // New ID for completion notifications

    companion object {
        const val EXTRA_REMAINING_MILLIS = "remainingMillis"
        const val EXTRA_SESSION_TYPE = "sessionType"
        const val EXTRA_IS_RUNNING = "isRunning"
        const val ACTION_RESTORE_TIMER = "com.dong.focusflow.ACTION_RESTORE_TIMER"
        const val ACTION_SESSION_FINISHED = "com.dong.focusflow.ACTION_SESSION_FINISHED" // New action for session finished
        const val EXTRA_FINISHED_SESSION_TYPE = "finishedSessionType" // New extra for finished session type
    }

    private lateinit var notificationManager: NotificationManager

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannels() // Create both channels
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_SESSION_FINISHED -> {
                // If the action is for a finished session, show completion notification
                // Nếu hành động là cho một phiên đã hoàn thành, hiển thị thông báo hoàn thành
                val finishedSessionType = intent.getStringExtra(EXTRA_FINISHED_SESSION_TYPE)?.let {
                    PomodoroSessionType.valueOf(it)
                } ?: PomodoroSessionType.FOCUS
                showSessionCompletionNotification(finishedSessionType)
                stopSelf() // Stop the service after showing completion notification (if not needed for ongoing timer)
            }
            else -> {
                // Default action: update ongoing timer notification
                // Hành động mặc định: cập nhật thông báo hẹn giờ đang diễn ra
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
     * Tạo các kênh thông báo cho Android Oreo (API 26) trở lên.
     */
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Channel for ongoing timer
            // Kênh cho đồng hồ đang chạy
            val ongoingChannel = NotificationChannel(
                ONGOING_CHANNEL_ID,
                "Kênh hẹn giờ Pomodoro",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Hiển thị thông báo hẹn giờ Pomodoro đang chạy."
                setSound(null, null) // No sound for ongoing notification
                enableVibration(false)
            }

            // Channel for session completion (higher importance)
            // Kênh cho hoàn thành phiên (độ ưu tiên cao hơn)
            val completionChannel = NotificationChannel(
                COMPLETION_CHANNEL_ID,
                "Hoàn thành Pomodoro",
                NotificationManager.IMPORTANCE_HIGH // High importance for alert
            ).apply {
                description = "Thông báo khi một phiên Pomodoro hoặc nghỉ kết thúc."
                enableLights(true)
                lightColor = getColor(R.color.purple_200) // Replace with your desired color (e.g., from themes.xml)
                enableVibration(true)
                setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), null) // Default notification sound
            }

            notificationManager.createNotificationChannel(ongoingChannel)
            notificationManager.createNotificationChannel(completionChannel)
        }
    }

    /**
     * Updates the ongoing notification with the current timer state.
     * Cập nhật thông báo đang diễn ra với trạng thái hẹn giờ hiện tại.
     */
    private fun updateOngoingNotification(remainingMillis: Long, sessionType: PomodoroSessionType, isRunning: Boolean) {
        val minutes = (remainingMillis / 1000 / 60).toInt()
        val seconds = (remainingMillis / 1000 % 60).toInt()
        val formattedTime = String.format("%02d:%02d", minutes, seconds)

        val sessionTitle = when (sessionType) {
            PomodoroSessionType.FOCUS -> "Tập trung"
            PomodoroSessionType.SHORT_BREAK -> "Nghỉ ngắn"
            PomodoroSessionType.LONG_BREAK -> "Nghỉ dài"
        }

        val notificationText = if (isRunning) {
            "Thời gian còn lại: $formattedTime"
        } else {
            "Tạm dừng ($formattedTime)"
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
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Replace with your app's actual icon
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        startForeground(ONGOING_NOTIFICATION_ID, notification)
    }

    /**
     * Displays a notification when a Pomodoro session or break completes.
     * Hiển thị thông báo khi một phiên Pomodoro hoặc nghỉ kết thúc.
     */
    private fun showSessionCompletionNotification(finishedSessionType: PomodoroSessionType) {
        val title = when (finishedSessionType) {
            PomodoroSessionType.FOCUS -> "Phiên tập trung đã hoàn thành!"
            PomodoroSessionType.SHORT_BREAK -> "Thời gian nghỉ ngắn đã kết thúc!"
            PomodoroSessionType.LONG_BREAK -> "Thời gian nghỉ dài đã kết thúc!"
        }
        val message = "Bạn có thể bắt đầu phiên tiếp theo."

        // Intent to launch MainActivity when notification is tapped
        // Intent để khởi chạy MainActivity khi chạm vào thông báo
        val notificationIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            action = ACTION_RESTORE_TIMER // Still use this action to bring app to foreground
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
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Use a relevant small icon
            .setContentIntent(pendingIntent)
            .setAutoCancel(true) // Makes the notification dismissible when tapped
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Use HIGH priority for completion
            .setDefaults(NotificationCompat.DEFAULT_ALL) // Play default sound, vibrate, light
            .build()

        notificationManager.notify(COMPLETION_NOTIFICATION_ID, completionNotification)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(true)
        notificationManager.cancel(COMPLETION_NOTIFICATION_ID) // Also cancel completion notification if active
    }
}