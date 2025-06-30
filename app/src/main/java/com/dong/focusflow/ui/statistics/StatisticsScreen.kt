package com.dong.focusflow.ui.statistics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dong.focusflow.ui.theme.FocusFlowTheme
import java.time.format.DateTimeFormatter
import java.time.LocalDate
import com.dong.focusflow.data.local.entity.PomodoroSession
import com.dong.focusflow.data.local.entity.PomodoroSessionType

// Imports cho Vico 1.x
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.entryOf
import com.patrykandpatrick.vico.core.component.shape.Shapes
import com.patrykandpatrick.vico.core.component.shape.LineComponent

/**
 * Composable function for the Statistics screen.
 * Hàm Composable cho màn hình Thống kê.
 * Displays overall statistics, a chart of Pomodoros per day, and a list of completed sessions.
 * Hiển thị số liệu thống kê tổng thể, biểu đồ số Pomodoro mỗi ngày và danh sách các phiên đã hoàn thành.
 *
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
            TopAppBar(title = { Text("Thống kê Pomodoro") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Tổng số Pomodoro đã hoàn thành: $totalCompletedFocusSessionsCount",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tổng thời gian tập trung: ${totalFocusDuration} phút",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Số Pomodoro trong tháng ${currentMonth}/${currentYear}:",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
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
                Text("Chưa có dữ liệu thống kê cho tháng này.", modifier = Modifier.padding(16.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Lịch sử phiên Pomodoro:",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyColumn {
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
fun PomodoroSessionItem(session: PomodoroSession) {
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
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
