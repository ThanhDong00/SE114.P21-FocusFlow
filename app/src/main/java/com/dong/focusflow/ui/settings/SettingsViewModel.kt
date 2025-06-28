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

    // Settings StateFlows
    private val _focusTime = MutableStateFlow(SettingsDataStore.DEFAULT_FOCUS_TIME)
    val focusTime: StateFlow<Int> = _focusTime.asStateFlow()

    private val _shortBreakTime = MutableStateFlow(SettingsDataStore.DEFAULT_SHORT_BREAK_TIME)
    val shortBreakTime: StateFlow<Int> = _shortBreakTime.asStateFlow()

    private val _longBreakTime = MutableStateFlow(SettingsDataStore.DEFAULT_LONG_BREAK_TIME)
    val longBreakTime: StateFlow<Int> = _longBreakTime.asStateFlow()

    private val _shortBreaksBeforeLongBreak = MutableStateFlow(SettingsDataStore.DEFAULT_SHORT_BREAKS_BEFORE_LONG_BREAK)
    val shortBreaksBeforeLongBreak: StateFlow<Int> = _shortBreaksBeforeLongBreak.asStateFlow()

    // UI State
    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            getPomodoroSettingsUseCase.getFocusTime().collect { 
                _focusTime.value = it 
            }
        }
        
        viewModelScope.launch {
            getPomodoroSettingsUseCase.getShortBreakTime().collect { 
                _shortBreakTime.value = it 
            }
        }
        
        viewModelScope.launch {
            getPomodoroSettingsUseCase.getLongBreakTime().collect { 
                _longBreakTime.value = it 
            }
        }
        
        viewModelScope.launch {
            getPomodoroSettingsUseCase.getShortBreaksBeforeLongBreak().collect { 
                _shortBreaksBeforeLongBreak.value = it 
            }
        }
    }

    /**
     * Saves the focus time setting.
     */
    fun saveFocusTime(time: Int) {
        saveSetting { savePomodoroSettingsUseCase.saveFocusTime(time) }
    }

    /**
     * Saves the short break time setting.
     */
    fun saveShortBreakTime(time: Int) {
        saveSetting { savePomodoroSettingsUseCase.saveShortBreakTime(time) }
    }

    /**
     * Saves the long break time setting.
     */
    fun saveLongBreakTime(time: Int) {
        saveSetting { savePomodoroSettingsUseCase.saveLongBreakTime(time) }
    }

    /**
     * Saves the number of short breaks before a long break setting.
     */
    fun saveShortBreaksBeforeLongBreak(count: Int) {
        saveSetting { savePomodoroSettingsUseCase.saveShortBreaksBeforeLongBreak(count) }
    }

    private fun saveSetting(saveAction: suspend () -> Unit) {
        viewModelScope.launch {
            saveAction()
            _userMessage.value = "Settings saved successfully!"
        }
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    companion object {
        private const val SUCCESS_MESSAGE = "Settings saved successfully!"
    }
}