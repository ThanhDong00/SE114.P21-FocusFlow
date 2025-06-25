package com.dong.focusflow.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.dong.focusflow.data.local.dao.PomodoroDao
import com.dong.focusflow.data.local.entity.PomodoroSession
import com.dong.focusflow.data.local.converter.LocalDateTimeConverter

@Database(
    entities = [PomodoroSession::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(LocalDateTimeConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun pomodoroDao() : PomodoroDao
}