package com.dong.focusflow.domain.usecase

import com.dong.focusflow.data.repository.PomodoroRepository
import javax.inject.Inject

class SavePomodoroSettingsUseCase @Inject constructor(
    private val repository: PomodoroRepository
){
    suspend fun saveFocusTime(time: Int) = repository.saveFocusTimeSetting(time)

    suspend fun saveShortBreakTime(time: Int) = repository.saveShortBreakTimeSetting(time)

    suspend fun saveLongBreakTime(time: Int) = repository.saveLongBreakTimeSetting(time)
}