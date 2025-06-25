package com.dong.focusflow.ui.pomodoro

import android.os.CountDownTimer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    private val getPomodoroSettingsUseCase: GetPomodoroSettingsUseCase,
    private val recordPomodoroSessionUseCase: RecordPomodoroSessionUseCase
) : ViewModel() {

    private val _focusTime = MutableStateFlow(25) // Default value, will be updated by DataStore
    private val _shortBreakTime = MutableStateFlow(5) // Default value
    private val _longBreakTime = MutableStateFlow(15) // Default value

    // StateFlow UI được hiển thị ra Composables
    private val _timerState = MutableStateFlow(TimerState.STOPPED)
    val timerState: StateFlow<TimerState> = _timerState.asStateFlow()

    private val _remainingTime = MutableStateFlow(0L) // milliseconds
    val remainingTime: StateFlow<Long> = _remainingTime.asStateFlow()

    private val _currentSessionType = MutableStateFlow(PomodoroSessionType.FOCUS)
    val currentSessionType: StateFlow<PomodoroSessionType> = _currentSessionType.asStateFlow()

    private var countDownTimer: CountDownTimer? = null
    private var sessionStartTime: LocalDateTime? = null
    private var sessionDurationMillis: Long = 0L // Actual duration of the session when started, in milliseconds
    private var isManualSkip: Boolean = false // Flag to differentiate between natural finish and manual skip/reset

    init {
        // Collect settings from DataStore via UseCases
        // Thu thập cài đặt từ DataStore thông qua UseCase
        viewModelScope.launch {
            getPomodoroSettingsUseCase.getFocusTime().collect { time ->
                _focusTime.value = time
                // Reset timer with new focus time if currently stopped and is focus session
                if (_timerState.value == TimerState.STOPPED && _currentSessionType.value == PomodoroSessionType.FOCUS) {
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

        // Initialize remaining time with default focus time until settings are loaded
        // Khởi tạo thời gian còn lại với thời gian tập trung mặc định cho đến khi cài đặt được tải
        _remainingTime.value = _focusTime.value * 60L * 1000L
    }

    fun startTimer() {
        if (_timerState.value == TimerState.RUNNING) return // Prevent starting if already running

        // Nếu bắt đầu một phiên mới hoặc tiếp tục từ trạng thái dừng/hoàn thành
        if (_timerState.value == TimerState.STOPPED || _timerState.value == TimerState.FINISHED) {
            sessionStartTime = LocalDateTime.now() // Record start time for new session
            sessionDurationMillis = when (_currentSessionType.value) {
                PomodoroSessionType.FOCUS -> _focusTime.value * 60L * 1000L
                PomodoroSessionType.SHORT_BREAK -> _shortBreakTime.value * 60L * 1000L
                PomodoroSessionType.LONG_BREAK -> _longBreakTime.value * 60L * 1000L
            }
            // Nếu đồng hồ đã hoàn thành, đặt lại thời gian còn lại về toàn bộ thời lượng của loại phiên mới
            if (_timerState.value == TimerState.FINISHED) {
                _remainingTime.value = sessionDurationMillis
            }
        } else if (_timerState.value == TimerState.PAUSED) {
            // Nếu tiếp tục từ trạng thái tạm dừng, sessionStartTime và sessionDurationMillis đã được đặt
        }


        countDownTimer?.cancel() // Cancel any existing timer
        countDownTimer = object : CountDownTimer(_remainingTime.value, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                _remainingTime.value = millisUntilFinished
            }

            override fun onFinish() {
                _remainingTime.value = 0L
                _timerState.value = TimerState.FINISHED // Indicate timer finished naturally
                recordSession(true) // Session completed

                // Automatically switch to the next session type
                // Tự động chuyển sang loại phiên tiếp theo
                when (_currentSessionType.value) {
                    PomodoroSessionType.FOCUS -> {
                        // For simplicity, always switch to SHORT_BREAK.
                        // Advanced logic for alternating short/long breaks after N focus sessions could go here.
                        // Để đơn giản, luôn chuyển sang NGHỈ NGẮN.
                        // Logic nâng cao để xen kẽ nghỉ ngắn/nghỉ dài sau N phiên tập trung có thể đặt ở đây.
                        _currentSessionType.value = PomodoroSessionType.SHORT_BREAK
                        _remainingTime.value = _shortBreakTime.value * 60L * 1000L
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
                _timerState.value = TimerState.STOPPED // Reset state after finishing and preparing for next session
            }
        }.start()
        _timerState.value = TimerState.RUNNING // Set timer state to running
        isManualSkip = false // Reset skip flag
    }

    fun pauseTimer() {
        countDownTimer?.cancel()
        _timerState.value = TimerState.PAUSED
    }

    fun resetTimer() {
        countDownTimer?.cancel()
        // Only record if it was running/paused and not already finished
        // Chỉ ghi lại nếu nó đang chạy/tạm dừng và chưa kết thúc
        if (_timerState.value == TimerState.RUNNING || _timerState.value == TimerState.PAUSED) {
            recordSession(false) // Session not completed if reset prematurely
        }

        // Reset remaining time based on current session type
        // Đặt lại thời gian còn lại dựa trên loại phiên hiện tại
        _remainingTime.value = when (_currentSessionType.value) {
            PomodoroSessionType.FOCUS -> _focusTime.value * 60L * 1000L
            PomodoroSessionType.SHORT_BREAK -> _shortBreakTime.value * 60L * 1000L
            PomodoroSessionType.LONG_BREAK -> _longBreakTime.value * 60L * 1000L
        }
        _timerState.value = TimerState.STOPPED // Set state to stopped
        isManualSkip = false // Reset skip flag
    }

    //Bỏ qua phiên Pomodoro hiện tại và chuyển sang phiên tiếp theo.
    fun skipSession() {
        countDownTimer?.cancel()
        isManualSkip = true // Mark as manual skip
        // Only record if it was running/paused and not already finished
        if (_timerState.value == TimerState.RUNNING || _timerState.value == TimerState.PAUSED) {
            recordSession(false) // Session not completed if skipped
        }

        // Switch to the next session type
        // Chuyển sang loại phiên tiếp theo
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
                _currentSessionType.value = PomodoroSessionType.FOCUS
                _remainingTime.value = _focusTime.value * 60L * 1000L
            }
        }
        _timerState.value = TimerState.STOPPED // Set state to stopped
    }

    /**
     * Records the Pomodoro session details to the database.
     * Ghi lại chi tiết phiên Pomodoro vào cơ sở dữ liệu.
     * @param completed True if the session finished naturally, false if skipped/reset prematurely.
     */
    private fun recordSession(completed: Boolean) {
        sessionStartTime?.let { start ->
            val endTime = LocalDateTime.now()
            // Calculate actual duration based on elapsed time from start time.
            // If skipped/reset, duration is from start to current time.
            // If completed, duration is the full intended session duration.
            val actualDurationMillis = Duration.between(start, endTime).toMillis()

            // Only record if the duration is significant, e.g., more than a few seconds
            // Chỉ ghi lại nếu thời lượng đáng kể, ví dụ: hơn vài giây
            if (actualDurationMillis > 5000) { // Record if session lasted more than 5 seconds
                val session = PomodoroSession(
                    startTime = start,
                    endTime = endTime,
                    durationMinutes = (actualDurationMillis / (1000 * 60)).toInt(), // Convert to minutes
                    type = _currentSessionType.value,
                    completed = completed
                )
                viewModelScope.launch {
                    recordPomodoroSessionUseCase(session)
                }
            }
        }
    }

    enum class TimerState {
        RUNNING, PAUSED, STOPPED, FINISHED
    }
}