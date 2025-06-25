package com.dong.focusflow.ui.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dong.focusflow.data.local.entity.PomodoroSession
import com.dong.focusflow.domain.usecase.GetStatisticsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val getStatisticsUseCase: GetStatisticsUseCase
) : ViewModel() {
    val completedFocusSessions: StateFlow<List<PomodoroSession>> =
        getStatisticsUseCase.getCompletedFocusSessions()
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000), // Start collecting when there are subscribers, stop after 5 seconds of inactivity
                emptyList()
            )

    val totalCompletedFocusSessionsCount: StateFlow<Int> =
        getStatisticsUseCase.getTotalCompletedFocusSessionsCount()
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                0
            )

    val totalFocusDuration: StateFlow<Int> =
        getStatisticsUseCase.getTotalFocusDuration()
            .map { it ?: 0 } // Convert Int? to Int, defaulting to 0 if null
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                0
            )

    val pomodorosPerDay: StateFlow<Map<String, Int>> =
        completedFocusSessions.map { sessions ->
            sessions
                .groupBy { it.startTime.toLocalDate().toString() } // Group by date (e.g., "2023-10-27")
                .mapValues { it.value.size } // Count sessions per day
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyMap() // Initial value
        )
}