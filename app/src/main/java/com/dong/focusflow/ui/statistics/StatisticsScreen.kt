package com.dong.focusflow.ui.statistics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dong.focusflow.data.local.entity.PomodoroSession
import com.dong.focusflow.ui.theme.Blue50
import com.dong.focusflow.ui.theme.Blue900
import com.dong.focusflow.ui.theme.FocusFlowTheme
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.core.component.shape.LineComponent
import com.patrykandpatrick.vico.core.component.shape.Shapes
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.entryOf
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

/**
 * Composable function for the Statistics screen.
 * Displays overall statistics, a chart of Pomodoros per day, and a list of completed sessions.
 * @param viewModel The StatisticsViewModel instance, injected by Hilt.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val completedFocusSessions by viewModel.completedFocusSessions.collectAsState()
    val totalCompletedFocusSessionsCount by viewModel.totalCompletedFocusSessionsCount.collectAsState()
    val totalFocusDuration by viewModel.totalFocusDuration.collectAsState()
    val pomodorosPerDay by viewModel.pomodorosPerDay.collectAsState()

    // Calculate statistics for different time periods
    val now = LocalDate.now()
    val startOfWeek = now.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
    val startOfMonth = now.withDayOfMonth(1)
    val startOfYear = now.withDayOfYear(1)

    val weeklyPomodoros = completedFocusSessions.count { session ->
        session.startTime.toLocalDate() >= startOfWeek
    }

    val monthlyPomodoros = completedFocusSessions.count { session ->
        session.startTime.toLocalDate() >= startOfMonth
    }

    val yearlyPomodoros = completedFocusSessions.count { session ->
        session.startTime.toLocalDate() >= startOfYear
    }

    // Prepare data for the chart using ChartEntryModelProducer from Vico library
    // Filter data to show only current month
    val chartEntryModelProducer = ChartEntryModelProducer()
    val currentMonth = LocalDate.now().monthValue
    val currentYear = LocalDate.now().year

    // Filter pomodorosPerDay to only include current month
    val currentMonthData = pomodorosPerDay.entries.filter { entry ->
        try {
            val dateParts = entry.key.split("-") // Assuming format is "yyyy-MM-dd"
            if (dateParts.size >= 3) {
                val year = dateParts[0].toInt()
                val month = dateParts[1].toInt()
                year == currentYear && month == currentMonth
            } else false
        } catch (e: Exception) {
            false
        }
    }

    val sortedData = currentMonthData.sortedBy { it.key }
    val chartEntries = sortedData.mapIndexed { index, entry ->
        entryOf(index.toFloat(), entry.value.toFloat())
    }
    chartEntryModelProducer.setEntries(chartEntries)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Thống kê Pomodoro",
                        color = Blue900,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Blue50
                )
            )
        }
    ) { paddingValues ->
        val scrollState = rememberScrollState()
        val configuration = LocalConfiguration.current
        val screenHeight = configuration.screenHeightDp.dp
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Blue50)
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // Statistics Cards in 2x2 grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatisticsCard(
                    title = "Tuần này",
                    value = "$weeklyPomodoros",
                    modifier = Modifier.weight(1f)
                )
                StatisticsCard(
                    title = "Tháng này",
                    value = "$monthlyPomodoros",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatisticsCard(
                    title = "Năm này",
                    value = "$yearlyPomodoros",
                    modifier = Modifier.weight(1f)
                )
                StatisticsCard(
                    title = "Tất cả",
                    value = "$totalCompletedFocusSessionsCount",
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Số Pomodoro trong tháng ${currentMonth}/${currentYear}:",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp),
                color = Blue900
            )

            if (sortedData.isNotEmpty()) {
                Chart(
                    chart = columnChart(
                        columns = listOf(
                            LineComponent(
                                color = MaterialTheme.colorScheme.primary.toArgb(),
                                thicknessDp = 12f, // Giảm độ dày để hiển thị nhiều column hơn
                                shape = Shapes.rectShape
                            )
                        )
                    ),
                    chartModelProducer = chartEntryModelProducer,
                    startAxis = rememberStartAxis(
                        valueFormatter = { value, _ -> value.toInt().toString() }
                    ),
                    bottomAxis = rememberBottomAxis(
                        valueFormatter = { value, _ ->
                            if (value.toInt() in sortedData.indices) {
                                val dateString = sortedData[value.toInt()].key
                                // Hiển thị chỉ ngày vì đã filter theo tháng
                                try {
                                    val parts = dateString.split("-")
                                    if (parts.size >= 3) parts[2] else dateString // Chỉ hiển thị ngày
                                } catch (e: Exception) {
                                    dateString
                                }
                            } else ""
                        },
                        labelRotationDegrees = 0f // Không cần xoay vì chỉ hiển thị số ngày
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(vertical = 8.dp)
                )
            } else {
                Text(
                    "Chưa có dữ liệu thống kê cho tháng này.",
                    modifier = Modifier.padding(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Lịch sử phiên Pomodoro:",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyColumn(
                modifier = Modifier.height(screenHeight - 300.dp)
            ) {
                if (completedFocusSessions.isEmpty()) {
                    item {
                        Text("Chưa có phiên nào được ghi lại.", modifier = Modifier.padding(16.dp))
                    }
                } else {
                    items(completedFocusSessions) { session ->
                        PomodoroSessionItem(session = session)
                    }
                }
            }
        }
    }
}


@Composable
private fun StatisticsCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = Blue900
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                color = Blue900
            )
        }
    }
}

@Composable
fun PomodoroSessionItem(session: PomodoroSession) {
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Loại: ${session.type.name.replace("_", " ")}",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "Bắt đầu: ${session.startTime.format(formatter)}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Kết thúc: ${session.endTime.format(formatter)}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Thời lượng: ${session.durationMinutes} phút",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Hoàn thành: ${if (session.completed) "Có" else "Không"}",
                style = MaterialTheme.typography.bodyMedium,
                color = if (session.completed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 320)
@Composable
fun StatisticsScreenPreview() {
    FocusFlowTheme {
        StatisticsScreen()
    }
}
