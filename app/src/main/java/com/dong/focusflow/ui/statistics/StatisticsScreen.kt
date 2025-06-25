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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dong.focusflow.ui.theme.FocusFlowTheme
import java.time.format.DateTimeFormatter
import com.dong.focusflow.data.local.entity.PomodoroSession
import com.dong.focusflow.data.local.entity.PomodoroSessionType

// Imports cho Vico 1.x
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.entryOf

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
    // Chuẩn bị dữ liệu cho biểu đồ bằng ChartEntryModelProducer từ thư viện Vico
    val chartEntryModelProducer = ChartEntryModelProducer()
    // Sort data by date to ensure correct order in the chart
    // Sắp xếp dữ liệu theo ngày để đảm bảo đúng thứ tự trong biểu đồ
    val sortedData = pomodorosPerDay.entries.sortedBy { it.key }
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
                text = "Số Pomodoro theo ngày:",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (sortedData.isNotEmpty()) {
                Chart(
                    chart = columnChart(), // Dùng columnChart() cho Vico 1.16.1
                    chartModelProducer = chartEntryModelProducer,
                    startAxis = rememberStartAxis(
                        valueFormatter = { value, _ -> value.toInt().toString() }
                    ),
                    bottomAxis = rememberBottomAxis(
                        valueFormatter = { value, _ ->
                            if (value.toInt() in sortedData.indices) {
                                sortedData[value.toInt()].key
                            } else ""
                        }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(vertical = 8.dp)
                )
            } else {
                Text("Chưa có dữ liệu thống kê.", modifier = Modifier.padding(16.dp))
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
