package com.dong.focusflow

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.dong.focusflow.data.local.entity.PomodoroSessionType

class PomodoroNotificationService: Service() {
    private val CHANNEL_ID = "PomodoroTimerChannel"
    private val NOTIFICATION_ID = 101

    companion object {
        const val EXTRA_REMAINING_MILLIS = "remainingMillis"
        const val EXTRA_SESSION_TYPE = "sessionType"
        const val EXTRA_IS_RUNNING = "isRunning"
        const val ACTION_RESTORE_TIMER = "com.dong.focusflow.ACTION_RESTORE_TIMER"
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val remainingMillis = intent?.getLongExtra("remainingMillis", 0L) ?: 0L
        val sessionType = intent?.getStringExtra("sessionType")?.let {
            PomodoroSessionType.valueOf(it)
        } ?: PomodoroSessionType.FOCUS
        val isRunning = intent?.getBooleanExtra("isRunning", false) ?: false

        updateNotification(remainingMillis, sessionType, isRunning)

        // START_STICKY có nghĩa là nếu dịch vụ bị dừng, nó sẽ được tạo lại
        // nhưng intent đã khởi động nó sẽ không được gửi lại.
        return START_STICKY
    }

    /**
     * Tạo một kênh thông báo cho Android Oreo (API 26) trở lên.
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Kênh hẹn giờ Pomodoro"
            val descriptionText = "Hiển thị thông báo hẹn giờ Pomodoro"
            val importance = NotificationManager.IMPORTANCE_LOW // Use LOW for ongoing background task
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Cập nhật thông báo đang diễn ra với trạng thái hẹn giờ hiện tại.
     */
    private fun updateNotification(remainingMillis: Long, sessionType: PomodoroSessionType, isRunning: Boolean) {
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

        // Intent để khởi chạy MainActivity khi chạm vào thông báo
        val notificationIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            action = ACTION_RESTORE_TIMER
            // Truyền trạng thái hẹn giờ hiện tại dưới dạng extras
            putExtra(EXTRA_REMAINING_MILLIS, remainingMillis)
            putExtra(EXTRA_SESSION_TYPE, sessionType.name)
            putExtra(EXTRA_IS_RUNNING, isRunning)
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE // Use FLAG_UPDATE_CURRENT to update extras
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Pomodoro: $sessionTitle")
            .setContentText(notificationText)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Show app's icon
            .setContentIntent(pendingIntent) // Tap action
            .setOngoing(true) // Makes the notification non-dismissible
            .setPriority(NotificationCompat.PRIORITY_LOW) // Matches channel importance
            .build()

        startForeground(NOTIFICATION_ID, notification) // Start as a foreground service
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(true) // Remove notification when service is destroyed
    }
}