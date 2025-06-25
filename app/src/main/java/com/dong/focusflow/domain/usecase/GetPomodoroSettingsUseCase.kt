package com.dong.focusflow.domain.usecase

import com.dong.focusflow.data.repository.PomodoroRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPomodoroSettingsUseCase @Inject constructor(
    private val repository: PomodoroRepository
){
    fun getFocusTime(): Flow<Int> = repository.getFocusTimeSetting()

    fun getShortBreakTime(): Flow<Int> = repository.getShortBreakTimeSetting()

    fun getLongBreakTime(): Flow<Int> = repository.getLongBreakTimeSetting()

    fun getShortBreaksBeforeLongBreak(): Flow<Int> = repository.getShortBreaksBeforeLongBreakSetting()
}