package com.dong.focusflow.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(
    tableName = "pomodoro_sessions"
)
data class PomodoroSession(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0, // ID duy nhất cho mỗi phiên.
    val startTime: LocalDateTime, // Thời gian bắt đầu phiên.
    val endTime: LocalDateTime, // Thời gian kết thúc phiên.
    val durationMinutes: Int, // Thời lượng thực tế của phiên tính bằng phút.
    val type: PomodoroSessionType, //  Loại phiên (TẬP TRUNG, NGHỈ NGẮN, NGHỈ DÀI).
    val completed: Boolean //  Đánh dấu nếu phiên hoàn thành thành công.
)

enum class PomodoroSessionType {
    FOCUS, //  Phiên tập trung.
    SHORT_BREAK, // Phiên nghỉ ngắn.
    LONG_BREAK // Phiên nghỉ dài.
}