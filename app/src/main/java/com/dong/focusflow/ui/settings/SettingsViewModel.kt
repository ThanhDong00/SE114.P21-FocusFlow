package com.dong.focusflow.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dong.focusflow.data.local.datastore.SettingsDataStore
import com.dong.focusflow.domain.usecase.GetPomodoroSettingsUseCase
import com.dong.focusflow.domain.usecase.SavePomodoroSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getPomodoroSettingsUseCase: GetPomodoroSettingsUseCase,
    private val savePomodoroSettingsUseCase: SavePomodoroSettingsUseCase
) : ViewModel() {

    // MutableStateFlow để giữ các cài đặt hiện tại, được hiển thị dưới dạng StateFlow
    private val _focusTime = MutableStateFlow(SettingsDataStore.DEFAULT_FOCUS_TIME)
    val focusTime: StateFlow<Int> = _focusTime.asStateFlow()

    private val _shortBreakTime = MutableStateFlow(SettingsDataStore.DEFAULT_SHORT_BREAK_TIME)
    val shortBreakTime: StateFlow<Int> = _shortBreakTime.asStateFlow()

    private val _longBreakTime = MutableStateFlow(SettingsDataStore.DEFAULT_LONG_BREAK_TIME)
    val longBreakTime: StateFlow<Int> = _longBreakTime.asStateFlow()

    private val _shortBreaksBeforeLongBreak = MutableStateFlow(SettingsDataStore.DEFAULT_SHORT_BREAKS_BEFORE_LONG_BREAK)
    val shortBreaksBeforeLongBreak: StateFlow<Int> = _shortBreaksBeforeLongBreak.asStateFlow()

    init {
        // Thu thập cài đặt từ DataStore thông qua UseCase ngay khi ViewModel được tạo
        viewModelScope.launch {
            getPomodoroSettingsUseCase.getFocusTime().collect { _focusTime.value = it }
        }
        viewModelScope.launch {
            getPomodoroSettingsUseCase.getShortBreakTime().collect { _shortBreakTime.value = it }
        }
        viewModelScope.launch {
            getPomodoroSettingsUseCase.getLongBreakTime().collect { _longBreakTime.value = it }
        }
        viewModelScope.launch {
            getPomodoroSettingsUseCase.getShortBreaksBeforeLongBreak().collect { _shortBreaksBeforeLongBreak.value = it }
        }
    }

    /**
     * Lưu cài đặt thời gian tập trung mới.
     * @param time The new focus time in minutes.
     */
    fun saveFocusTime(time: Int) {
        viewModelScope.launch {
            savePomodoroSettingsUseCase.saveFocusTime(time)
        }
    }

    /**
     * Lưu cài đặt thời gian nghỉ ngắn mới.
     * @param time The new short break time in minutes.
     */
    fun saveShortBreakTime(time: Int) {
        viewModelScope.launch {
            savePomodoroSettingsUseCase.saveShortBreakTime(time)
        }
    }

    /**
     * Lưu cài đặt thời gian nghỉ dài mới.
     * @param time The new long break time in minutes.
     */
    fun saveLongBreakTime(time: Int) {
        viewModelScope.launch {
            savePomodoroSettingsUseCase.saveLongBreakTime(time)
        }
    }

    /**
     * Saves the new number of short breaks before a long break setting.
     * Lưu cài đặt số lần nghỉ ngắn mới trước khi nghỉ dài.
     * @param count The new count.
     */
    fun saveShortBreaksBeforeLongBreak(count: Int) {
        viewModelScope.launch {
            savePomodoroSettingsUseCase.saveShortBreaksBeforeLongBreak(count)
        }
    }
}