package com.dong.focusflow.domain.usecase

import com.dong.focusflow.data.local.entity.PomodoroSession
import com.dong.focusflow.data.repository.PomodoroRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetStatisticsUseCase @Inject constructor(
    private val repository: PomodoroRepository
) {
    fun getCompletedFocusSessions(): Flow<List<PomodoroSession>> {
        return repository.getCompletedFocusSessions()
    }

    fun getTotalCompletedFocusSessionsCount(): Flow<Int> {
        return repository.getTotalCompletedFocusSessionsCount()
    }

    fun getTotalFocusDuration(): Flow<Int?> {
        return repository.getTotalFocusDuration()
    }
}