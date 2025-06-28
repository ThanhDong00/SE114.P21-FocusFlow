package com.dong.focusflow.ui.pomodoro

import android.app.Application
import android.content.Intent
import android.os.CountDownTimer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dong.focusflow.PomodoroNotificationService
import com.dong.focusflow.data.local.entity.PomodoroSession
import com.dong.focusflow.data.local.entity.PomodoroSessionType
import com.dong.focusflow.domain.usecase.GetPomodoroSettingsUseCase
import com.dong.focusflow.domain.usecase.RecordPomodoroSessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class PomodoroViewModel @Inject constructor(
    private val application: Application,
    private val getPomodoroSettingsUseCase: GetPomodoroSettingsUseCase,
    private val recordPomodoroSessionUseCase: RecordPomodoroSessionUseCase
) : ViewModel() {

    // Settings StateFlows
    private val _focusTime = MutableStateFlow(25)
    private val _shortBreakTime = MutableStateFlow(5)
    private val _longBreakTime = MutableStateFlow(15)
    private val _shortBreaksBeforeLongBreak = MutableStateFlow(4)

    // Timer StateFlows
    private val _timerState = MutableStateFlow(TimerState.STOPPED)
    val timerState: StateFlow<TimerState> = _timerState.asStateFlow()

    private val _remainingTime = MutableStateFlow(0L)
    val remainingTime: StateFlow<Long> = _remainingTime.asStateFlow()

    private val _currentSessionType = MutableStateFlow(PomodoroSessionType.FOCUS)
    val currentSessionType: StateFlow<PomodoroSessionType> = _currentSessionType.asStateFlow()

    // Private properties
    private var countDownTimer: CountDownTimer? = null
    private var sessionStartTime: LocalDateTime? = null
    private var sessionDurationMillis: Long = 0L
    private var isManualSkip: Boolean = false
    private var completedFocusSessionCount: Int = 0

    enum class TimerState {
        RUNNING, PAUSED, STOPPED, FINISHED
    }

    init {
        initializeSettings()
    }

    private fun initializeSettings() {
        viewModelScope.launch {
            getPomodoroSettingsUseCase.getFocusTime().collect { time ->
                _focusTime.value = time
                if (_timerState.value == TimerState.STOPPED && _currentSessionType.value == PomodoroSessionType.FOCUS && _remainingTime.value == 0L) {
                    _remainingTime.value = time * 60L * 1000L
                }
            }
        }
        
        viewModelScope.launch {
            getPomodoroSettingsUseCase.getShortBreakTime().collect { time ->
                _shortBreakTime.value = time
            }
        }
        
        viewModelScope.launch {
            getPomodoroSettingsUseCase.getLongBreakTime().collect { time ->
                _longBreakTime.value = time
            }
        }
        
        viewModelScope.launch {
            getPomodoroSettingsUseCase.getShortBreaksBeforeLongBreak().collect { count ->
                _shortBreaksBeforeLongBreak.value = count
            }
        }
    }

    /**
     * Restores timer state from external source (e.g., notification tap).
     * Should only be called once when screen is created with initial data.
     */
    fun restoreTimerState(remainingMillis: Long, sessionType: PomodoroSessionType, isRunning: Boolean) {
        if (_timerState.value == TimerState.STOPPED && remainingMillis > 0L) {
            _remainingTime.value = remainingMillis
            _currentSessionType.value = sessionType
            _timerState.value = if (isRunning) TimerState.PAUSED else TimerState.STOPPED
            
            countDownTimer?.cancel()

            updateNotificationService()
            
            if (isRunning) {
                startTimer()
            }
        }
    }

    fun startTimer() {
        if (_timerState.value == TimerState.RUNNING) return

        setupSessionIfNeeded()
        startCountDownTimer()
        
        _timerState.value = TimerState.RUNNING
        isManualSkip = false
        updateNotificationService()
    }

    private fun setupSessionIfNeeded() {
        if (_timerState.value == TimerState.STOPPED || _timerState.value == TimerState.FINISHED) {
            sessionStartTime = LocalDateTime.now()
            sessionDurationMillis = getSessionDurationMillis(_currentSessionType.value)
            
            if (_timerState.value == TimerState.FINISHED) {
                _remainingTime.value = sessionDurationMillis
            }
        }
    }

    private fun getSessionDurationMillis(sessionType: PomodoroSessionType): Long {
        return when (sessionType) {
            PomodoroSessionType.FOCUS -> _focusTime.value * 60L * 1000L
            PomodoroSessionType.SHORT_BREAK -> _shortBreakTime.value * 60L * 1000L
            PomodoroSessionType.LONG_BREAK -> _longBreakTime.value * 60L * 1000L
        }
    }

    private fun startCountDownTimer() {
        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(_remainingTime.value, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                _remainingTime.value = millisUntilFinished
                updateNotificationService()
            }

            override fun onFinish() {
                handleTimerFinish()
            }
        }.start()
    }

    private fun handleTimerFinish() {
        _remainingTime.value = 0L
        _timerState.value = TimerState.FINISHED
        recordSession(completed = true)

        if (_currentSessionType.value == PomodoroSessionType.FOCUS) {
            completedFocusSessionCount++
        }

        switchToNextSession()
        _timerState.value = TimerState.STOPPED
        updateNotificationService()
    }

    private fun switchToNextSession() {
        when (_currentSessionType.value) {
            PomodoroSessionType.FOCUS -> {
                if (completedFocusSessionCount % _shortBreaksBeforeLongBreak.value == 0) {
                    _currentSessionType.value = PomodoroSessionType.LONG_BREAK
                    _remainingTime.value = _longBreakTime.value * 60L * 1000L
                } else {
                    _currentSessionType.value = PomodoroSessionType.SHORT_BREAK
                    _remainingTime.value = _shortBreakTime.value * 60L * 1000L
                }
            }
            PomodoroSessionType.SHORT_BREAK -> {
                _currentSessionType.value = PomodoroSessionType.FOCUS
                _remainingTime.value = _focusTime.value * 60L * 1000L
            }
            PomodoroSessionType.LONG_BREAK -> {
                _currentSessionType.value = PomodoroSessionType.FOCUS
                _remainingTime.value = _focusTime.value * 60L * 1000L
            }
        }
    }

    fun pauseTimer() {
        countDownTimer?.cancel()
        _timerState.value = TimerState.PAUSED
        updateNotificationService()
    }

    fun resetTimer() {
        countDownTimer?.cancel()
        
        if (shouldRecordSession()) {
            recordSession(completed = false)
        }

        resetToCurrentSessionType()
        _timerState.value = TimerState.STOPPED
        isManualSkip = false
        completedFocusSessionCount = 0
        stopNotificationService()
    }

    private fun shouldRecordSession(): Boolean {
        return _timerState.value == TimerState.RUNNING || _timerState.value == TimerState.PAUSED
    }

    private fun resetToCurrentSessionType() {
        _remainingTime.value = getSessionDurationMillis(_currentSessionType.value)
    }

    fun skipSession() {
        countDownTimer?.cancel()
        isManualSkip = true

        if (shouldRecordSession()) {
            recordSession(completed = false)
            
            if (_currentSessionType.value == PomodoroSessionType.FOCUS && completedFocusSessionCount > 0) {
                completedFocusSessionCount--
            }
        }

        skipToNextSession()
        _timerState.value = TimerState.STOPPED
        updateNotificationService()
    }

    private fun skipToNextSession() {
        when (_currentSessionType.value) {
            PomodoroSessionType.FOCUS -> {
                _currentSessionType.value = PomodoroSessionType.SHORT_BREAK
                _remainingTime.value = _shortBreakTime.value * 60L * 1000L
            }
            PomodoroSessionType.SHORT_BREAK -> {
                _currentSessionType.value = PomodoroSessionType.FOCUS
                _remainingTime.value = _focusTime.value * 60L * 1000L
            }
            PomodoroSessionType.LONG_BREAK -> {
                completedFocusSessionCount = 0
                _currentSessionType.value = PomodoroSessionType.FOCUS
                _remainingTime.value = _focusTime.value * 60L * 1000L
            }
        }
    }

    /**
     * Records Pomodoro session details to the database.
     */
    private fun recordSession(completed: Boolean) {
        sessionStartTime?.let { start ->
            val endTime = LocalDateTime.now()
            val actualDurationMillis = Duration.between(start, endTime).toMillis()

            if (actualDurationMillis > MIN_SESSION_DURATION_MILLIS) {
                val session = PomodoroSession(
                    startTime = start,
                    endTime = endTime,
                    durationMinutes = (actualDurationMillis / (1000 * 60)).toInt(),
                    type = _currentSessionType.value,
                    completed = completed
                )
                
                viewModelScope.launch {
                    recordPomodoroSessionUseCase(session)
                }
            }
        }
    }

    /**
     * Updates the notification service with current timer state.
     */
    private fun updateNotificationService() {
        val intent = Intent(application, PomodoroNotificationService::class.java).apply {
            putExtra("remainingMillis", _remainingTime.value)
            putExtra("sessionType", _currentSessionType.value.name)
            putExtra("isRunning", _timerState.value == TimerState.RUNNING)
        }
        application.startForegroundService(intent)
    }

    /**
     * Stops the notification service.
     */
    private fun stopNotificationService() {
        val intent = Intent(application, PomodoroNotificationService::class.java)
        application.stopService(intent)
    }

    override fun onCleared() {
        super.onCleared()
        countDownTimer?.cancel()
        stopNotificationService()
    }

    companion object {
        private const val MIN_SESSION_DURATION_MILLIS = 5000L // 5 seconds
    }
}