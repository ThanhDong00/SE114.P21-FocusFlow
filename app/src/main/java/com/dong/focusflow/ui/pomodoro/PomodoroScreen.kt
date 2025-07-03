package com.dong.focusflow.ui.pomodoro

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ChipColors
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dong.focusflow.PomodoroNotificationService
import com.dong.focusflow.R
import com.dong.focusflow.data.local.entity.PomodoroSessionType
import com.dong.focusflow.ui.theme.FocusFlowTheme
import com.dong.focusflow.ui.theme.Green50
import com.dong.focusflow.ui.theme.Green600
import com.dong.focusflow.ui.theme.Green900
import com.dong.focusflow.ui.theme.Red50
import com.dong.focusflow.ui.theme.Red600
import com.dong.focusflow.ui.theme.Red900

@Composable
fun PomodoroScreen(
    viewModel: PomodoroViewModel = hiltViewModel(),
    initialRemainingMillis: Long = 0L,
    initialSessionType: PomodoroSessionType? = null,
    initialIsRunning: Boolean = false,
    initialAction: String? = null
) {
    // Thu thập StateFlow từ ViewModel để quan sát các thay đổi trong trạng thái UI
    val remainingTime by viewModel.remainingTime.collectAsState()
    val timerState by viewModel.timerState.collectAsState()
    val currentSessionType by viewModel.currentSessionType.collectAsState()

    LaunchedEffect(key1 = initialAction) {
        if (initialAction == PomodoroNotificationService.ACTION_RESTORE_TIMER && initialRemainingMillis > 0) {
            viewModel.restoreTimerState(
                remainingMillis = initialRemainingMillis,
                sessionType = initialSessionType ?: PomodoroSessionType.FOCUS, // Default to FOCUS if null
                isRunning = initialIsRunning
            )
        }
    }

    // Tính toán phút và giây từ mili giây còn lại
    val minutes = (remainingTime / 1000 / 60).toInt()
    val seconds = (remainingTime / 1000 % 60).toInt()

    // Định dạng thời gian để hiển thị (ví dụ: "05:00")
    val displayTime = String.format("%02d\n%02d", minutes, seconds)
    
    // Responsive font size based on screen width
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    val fontSize = when {
        screenWidth < 360 -> 180.sp // Màn hình nhỏ
        screenWidth < 480 -> 220.sp // Màn hình trung bình
        else -> 256.sp // Màn hình lớn
    }
    val lineHeight = fontSize * 0.8f // Line height bằng 80% của font size
    
    val backgroundColor = when (currentSessionType) {
        PomodoroSessionType.FOCUS -> Red50
        PomodoroSessionType.SHORT_BREAK -> Green50
        PomodoroSessionType.LONG_BREAK -> Green50
    }
    val textColor = when (currentSessionType) {
        PomodoroSessionType.FOCUS -> Red900
        PomodoroSessionType.SHORT_BREAK -> Green900
        PomodoroSessionType.LONG_BREAK -> Green900
    }
    val buttonBackgroundColor = when (currentSessionType) {
        PomodoroSessionType.FOCUS -> Red600
        PomodoroSessionType.SHORT_BREAK -> Green600
        PomodoroSessionType.LONG_BREAK -> Green600
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        AssistChip(
            onClick = {},
            label = {
                Text(
                    text = when (currentSessionType) {
                        PomodoroSessionType.FOCUS -> "TẬP TRUNG"
                        PomodoroSessionType.SHORT_BREAK -> "NGHỈ NGẮN"
                        PomodoroSessionType.LONG_BREAK -> "NGHỈ DÀI"
                    },
                    fontSize = 24.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            },
            leadingIcon = {
                Icon(
                    painter = when (currentSessionType) {
                        PomodoroSessionType.FOCUS -> painterResource(R.drawable.baseline_business_center_24)
                        PomodoroSessionType.SHORT_BREAK -> painterResource(R.drawable.baseline_coffee_24)
                        PomodoroSessionType.LONG_BREAK -> painterResource(R.drawable.baseline_grass_24)
                    },
                    contentDescription = "Localized description",
                    modifier = Modifier.size(24.dp),
                )
            },
            colors = AssistChipDefaults.assistChipColors(
                containerColor = buttonBackgroundColor,
                labelColor = textColor,
                leadingIconContentColor = textColor,
            ),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(2.dp, textColor),
            modifier = Modifier
                .padding(top = 16.dp)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {

            Text(
                text = displayTime,
                fontSize = fontSize,
                fontWeight = FontWeight(850),
                fontFamily = FontFamily.Monospace,
                color = textColor,
                lineHeight = lineHeight,
                style = TextStyle(
                    platformStyle = PlatformTextStyle(includeFontPadding = false),
                    lineHeight = lineHeight
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth() // Make row fill width
                .padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.SpaceAround, // Phân bổ đều các nút
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Restart
            Button(
                onClick = { viewModel.resetTimer() },
                enabled = timerState != PomodoroViewModel.TimerState.STOPPED,
                modifier = Modifier
                    .size(64.dp) // Kích thước nút
                    .weight(1f) // Chia đều không gian
                    .padding(horizontal = 8.dp),
                shape = RoundedCornerShape(20.dp), // Bo tròn góc
                contentPadding = PaddingValues(0.dp), // Bỏ padding mặc định
                colors = ButtonDefaults.buttonColors(
                    containerColor  = buttonBackgroundColor,
                    contentColor = textColor
                )
            ) {
                Icon(
                    painter = painterResource(R.drawable.baseline_restart_alt_24),
                    contentDescription = "Restart",
                    modifier = Modifier.size(32.dp)
                )
            }

            // Start / Pause
            Button(
                onClick = {
                    when (timerState) {
                        PomodoroViewModel.TimerState.RUNNING -> viewModel.pauseTimer() // If running, pause
                        else -> viewModel.startTimer()
                    }
                },
                modifier = Modifier
                    .size(96.dp)
                    .weight(1.5f)
                    .padding(horizontal = 8.dp),
                shape = RoundedCornerShape(24.dp),
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor  = buttonBackgroundColor,
                    contentColor = textColor
                )
            ) {
                Icon(
                    painter = if (timerState == PomodoroViewModel.TimerState.RUNNING) painterResource(
                        R.drawable.baseline_play_arrow_24
                    ) else painterResource(R.drawable.baseline_pause_24),
                    contentDescription = if (timerState == PomodoroViewModel.TimerState.RUNNING) "Tạm dừng" else "Bắt đầu",
                    modifier = Modifier.size(48.dp),
                )
            }

            // Skip
            Button(
                onClick = { viewModel.skipSession() },
                modifier = Modifier
                    .size(64.dp)
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor  = buttonBackgroundColor,
                    contentColor = textColor
                )
            ) {
                Icon(
                    painter = painterResource(R.drawable.baseline_skip_next_24),
                    contentDescription = "Skip",
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 320)
@Composable
fun PomodoroScreenPreview() {
    FocusFlowTheme {
        PomodoroScreen()
    }
}