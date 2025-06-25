package com.dong.focusflow.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dong.focusflow.ui.theme.FocusFlowTheme // Đảm bảo đúng tên theme của bạn

/**
 * Composable function for the Settings screen.
 * Hàm Composable cho màn hình Cài đặt.
 * Allows users to configure focus time, short break time, and long break time.
 * Cho phép người dùng cấu hình thời gian tập trung, thời gian nghỉ ngắn và thời gian nghỉ dài.
 *
 * @param viewModel The SettingsViewModel instance, injected by Hilt.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel() // Hilt will provide the ViewModel
) {
    // Collect settings as State from the ViewModel
    // Thu thập cài đặt dưới dạng State từ ViewModel
    val focusTime by viewModel.focusTime.collectAsState()
    val shortBreakTime by viewModel.shortBreakTime.collectAsState()
    val longBreakTime by viewModel.longBreakTime.collectAsState()

    // Use mutable states for text fields to allow editing
    // Sử dụng các trạng thái có thể thay đổi cho các trường văn bản để cho phép chỉnh sửa
    var focusTimeInput by remember { mutableStateOf(focusTime.toString()) }
    var shortBreakTimeInput by remember { mutableStateOf(shortBreakTime.toString()) }
    var longBreakTimeInput by remember { mutableStateOf(longBreakTime.toString()) }

    // Update input fields when settings change from DataStore (e.g., initial load or external change)
    // Cập nhật các trường nhập liệu khi cài đặt thay đổi từ DataStore (ví dụ: tải ban đầu hoặc thay đổi bên ngoài)
    LaunchedEffect(focusTime) { focusTimeInput = focusTime.toString() }
    LaunchedEffect(shortBreakTime) { shortBreakTimeInput = shortBreakTime.toString() }
    LaunchedEffect(longBreakTime) { longBreakTimeInput = longBreakTime.toString() }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Cài đặt Pomodoro") }) // Top app bar title
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Apply padding from Scaffold
                .padding(16.dp) // Additional padding
        ) {
            // OutlinedTextField for Focus Time setting
            // OutlinedTextField cho cài đặt Thời gian tập trung
            OutlinedTextField(
                value = focusTimeInput,
                onValueChange = { newValue ->
                    focusTimeInput = newValue.filter { it.isDigit() } // Only allow digits
                },
                label = { Text("Thời gian tập trung (phút)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), // Numeric keyboard
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp)) // Vertical space

            // OutlinedTextField for Short Break Time setting
            // OutlinedTextField cho cài đặt Thời gian nghỉ ngắn
            OutlinedTextField(
                value = shortBreakTimeInput,
                onValueChange = { newValue ->
                    shortBreakTimeInput = newValue.filter { it.isDigit() }
                },
                label = { Text("Thời gian nghỉ ngắn (phút)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            // OutlinedTextField for Long Break Time setting
            // OutlinedTextField cho cài đặt Thời gian nghỉ dài
            OutlinedTextField(
                value = longBreakTimeInput,
                onValueChange = { newValue ->
                    longBreakTimeInput = newValue.filter { it.isDigit() }
                },
                label = { Text("Thời gian nghỉ dài (phút)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Button to save all settings
            // Nút để lưu tất cả cài đặt
            Button(
                onClick = {
                    // Convert input to Int and save via ViewModel
                    // Chuyển đổi đầu vào thành Int và lưu qua ViewModel
                    focusTimeInput.toIntOrNull()?.let { viewModel.saveFocusTime(it) }
                    shortBreakTimeInput.toIntOrNull()?.let { viewModel.saveShortBreakTime(it) }
                    longBreakTimeInput.toIntOrNull()?.let { viewModel.saveLongBreakTime(it) }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = "Lưu cài đặt")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Lưu cài đặt")
            }
        }
    }
}

/**
 * Preview function for SettingsScreen Composable.
 * Hàm Preview cho Composable SettingsScreen.
 */
@Preview(showBackground = true, widthDp = 320)
@Composable
fun SettingsScreenPreview() {
    FocusFlowTheme { // Use your app's theme for the preview
        SettingsScreen()
    }
}