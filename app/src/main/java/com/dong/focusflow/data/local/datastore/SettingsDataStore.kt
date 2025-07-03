package com.dong.focusflow.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsDataStore @Inject constructor(@ApplicationContext private val context: Context) {
    companion object {
        val FOCUS_TIME_KEY = intPreferencesKey("focus_time")
        val SHORT_BREAK_TIME_KEY = intPreferencesKey("short_break_time")
        val LONG_BREAK_TIME_KEY = intPreferencesKey("long_break_time")
        val SHORT_BREAKS_BEFORE_LONG_BREAK_KEY = intPreferencesKey("short_breaks_before_long_break")

        const val DEFAULT_FOCUS_TIME = 25
        const val DEFAULT_SHORT_BREAK_TIME = 5
        const val DEFAULT_LONG_BREAK_TIME = 15
        const val DEFAULT_SHORT_BREAKS_BEFORE_LONG_BREAK = 4
    }

    val focusTime: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[FOCUS_TIME_KEY] ?: DEFAULT_FOCUS_TIME
        }

    val shortBreakTime: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[SHORT_BREAK_TIME_KEY] ?: DEFAULT_SHORT_BREAK_TIME
        }

    val longBreakTime: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[LONG_BREAK_TIME_KEY] ?: DEFAULT_LONG_BREAK_TIME
        }

    val shortBreaksBeforeLongBreak: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[SHORT_BREAKS_BEFORE_LONG_BREAK_KEY] ?: DEFAULT_SHORT_BREAKS_BEFORE_LONG_BREAK
        }

    /**
     * Lưu cài đặt thời gian tập trung.
     */
    suspend fun saveFocusTime(time: Int) {
        context.dataStore.edit { preferences ->
            preferences[FOCUS_TIME_KEY] = time
        }
    }

    /**
     * Lưu cài đặt thời gian nghỉ ngắn.
     */
    suspend fun saveShortBreakTime(time: Int) {
        context.dataStore.edit { preferences ->
            preferences[SHORT_BREAK_TIME_KEY] = time
        }
    }

    /**
     * Lưu cài đặt thời gian nghỉ dài.
     */
    suspend fun saveLongBreakTime(time: Int) {
        context.dataStore.edit { preferences ->
            preferences[LONG_BREAK_TIME_KEY] = time
        }
    }

    /**
     * Lưu cài đặt số lần nghỉ ngắn trước khi nghỉ dài.
     */
    suspend fun saveShortBreaksBeforeLongBreak(count: Int) {
        context.dataStore.edit { preferences ->
            preferences[SHORT_BREAKS_BEFORE_LONG_BREAK_KEY] = count
        }
    }
}