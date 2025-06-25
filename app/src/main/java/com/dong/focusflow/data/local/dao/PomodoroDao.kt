package com.dong.focusflow.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.dong.focusflow.data.local.entity.PomodoroSession
import kotlinx.coroutines.flow.Flow

@Dao
interface PomodoroDao {
    @Insert
    suspend fun insertSession(session: PomodoroSession)

    @Query("SELECT * FROM pomodoro_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<PomodoroSession>>

    @Query("SELECT * FROM pomodoro_sessions WHERE type = 'FOCUS' AND completed = 1 ORDER BY startTime DESC")
    fun getCompletedFocusSessions(): Flow<List<PomodoroSession>>

    @Query("SELECT COUNT(*) FROM pomodoro_sessions WHERE type = 'FOCUS' AND completed = 1")
    fun getTotalCompletedFocusSessionsCount(): Flow<Int>

    @Query("SELECT SUM(durationMinutes) FROM pomodoro_sessions WHERE type = 'FOCUS' AND completed = 1")
    fun getTotalFocusDuration(): Flow<Int?>
}