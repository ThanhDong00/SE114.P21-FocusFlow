package com.dong.focusflow.domain.usecase

import com.dong.focusflow.data.local.entity.PomodoroSession
import com.dong.focusflow.data.repository.PomodoroRepository
import javax.inject.Inject

class RecordPomodoroSessionUseCase @Inject constructor(
    private val repository: PomodoroRepository
) {
    suspend operator fun invoke(session: PomodoroSession) {
        repository.insertPomodoroSession(session)
    }
}