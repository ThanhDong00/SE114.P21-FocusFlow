package com.dong.focusflow.data.repository

import com.dong.focusflow.data.local.dao.PomodoroDao
import com.dong.focusflow.data.local.datastore.SettingsDataStore
import com.dong.focusflow.data.local.entity.PomodoroSession
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PomodoroRepository @Inject constructor(
    private val pomodoroDao: PomodoroDao,
    private val settingsDataStore: SettingsDataStore
) {
    suspend fun insertPomodoroSession(session: PomodoroSession) {
        pomodoroDao.insertSession(session)
    }

    fun getAllPomodoroSessions(): Flow<List<PomodoroSession>> {
        return pomodoroDao.getAllSessions()
    }

    fun getCompletedFocusSessions(): Flow<List<PomodoroSession>> {
        return pomodoroDao.getCompletedFocusSessions()
    }

    fun getTotalCompletedFocusSessionsCount(): Flow<Int> {
        return pomodoroDao.getTotalCompletedFocusSessionsCount()
    }

    fun getTotalFocusDuration(): Flow<Int?> {
        return pomodoroDao.getTotalFocusDuration()
    }

    fun getFocusTimeSetting(): Flow<Int> {
        return settingsDataStore.focusTime
    }

    fun getShortBreakTimeSetting(): Flow<Int> {
        return settingsDataStore.shortBreakTime
    }

    fun getLongBreakTimeSetting(): Flow<Int> {
        return settingsDataStore.longBreakTime
    }

    fun getShortBreaksBeforeLongBreakSetting(): Flow<Int> {
        return settingsDataStore.shortBreaksBeforeLongBreak
    }

    suspend fun saveFocusTimeSetting(time: Int) {
        settingsDataStore.saveFocusTime(time)
    }

    suspend fun saveShortBreakTimeSetting(time: Int) {
        settingsDataStore.saveShortBreakTime(time)
    }

    suspend fun saveLongBreakTimeSetting(time: Int) {
        settingsDataStore.saveLongBreakTime(time)
    }

    suspend fun saveShortBreaksBeforeLongBreakSetting(count: Int) {
        settingsDataStore.saveShortBreaksBeforeLongBreak(count)
    }
}