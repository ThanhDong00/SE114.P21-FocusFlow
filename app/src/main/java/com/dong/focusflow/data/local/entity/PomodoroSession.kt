package com.dong.focusflow.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(
    tableName = "pomodoro_sessions"
)
data class PomodoroSession(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0, // Unique ID for each session. ID duy nhất cho mỗi phiên.
    val startTime: LocalDateTime, // Start time of the session. Thời gian bắt đầu phiên.
    val endTime: LocalDateTime, // End time of the session. Thời gian kết thúc phiên.
    val durationMinutes: Int, // Actual duration of the session in minutes. Thời lượng thực tế của phiên tính bằng phút.
    val type: PomodoroSessionType, // Type of the session (FOCUS, SHORT_BREAK, LONG_BREAK). Loại phiên (TẬP TRUNG, NGHỈ NGẮN, NGHỈ DÀI).
    val completed: Boolean // Indicates if the session was completed successfully. Đánh dấu nếu phiên hoàn thành thành công.
)

enum class PomodoroSessionType {
    FOCUS, // Focus session. Phiên tập trung.
    SHORT_BREAK, // Short break session. Phiên nghỉ ngắn.
    LONG_BREAK // Long break session. Phiên nghỉ dài.
}