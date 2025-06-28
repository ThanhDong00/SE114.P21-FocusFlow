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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
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
            .fillMaxSize() // Fill the entire screen
            .background(backgroundColor)
            .padding(16.dp), // Add padding around the content
        horizontalAlignment = Alignment.CenterHorizontally, // Center content horizontally
        verticalArrangement = Arrangement.Center // Center content vertically
    ) {
        // Hiển thị loại phiên hiện tại (TẬP TRUNG, NGHỈ NGẮN, NGHỈ DÀI)
//        Text(
//            text = when (currentSessionType) {
//                PomodoroSessionType.FOCUS -> "TẬP TRUNG"
//                PomodoroSessionType.SHORT_BREAK -> "NGHỈ NGẮN"
//                PomodoroSessionType.LONG_BREAK -> "NGHỈ DÀI"
//            },
//            style = MaterialTheme.typography.headlineMedium, // Use a large heading style
//            color = MaterialTheme.colorScheme.primary // Use primary color from theme
//        )
//        Spacer(modifier = Modifier.height(32.dp)) // Add vertical space

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

        // Animated content for the timer display, for a smoother transition on time change
        // Nội dung động cho hiển thị đồng hồ, để chuyển đổi mượt mà hơn khi thời gian thay đổi
//        AnimatedContent(
//            targetState = displayTime,
//            transitionSpec = {
//                scaleIn() togetherWith scaleOut() // Scale in/out animation
//            }, label = "Time Animation"
//        ) { targetText ->
//            Text(
//                text = targetText,
//                style = MaterialTheme.typography.displayLarge, // Use an even larger display style
//                fontSize = 80.sp, // Explicitly set font size for prominence
//                color = MaterialTheme.colorScheme.onBackground // Text color
//            )
//        }
//        Spacer(modifier = Modifier.height(32.dp)) // Add vertical space
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {

            Text(
                text = displayTime,
                fontSize = 256.sp,
                fontWeight = FontWeight(850),
                fontFamily = FontFamily.Monospace,
                lineHeight = 256.sp,
                color = textColor,
                style = TextStyle(
                    platformStyle = PlatformTextStyle(includeFontPadding = false),
                    lineHeight = 256.sp
                ),
                modifier = Modifier.padding(0.dp)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth() // Make row fill width
                .padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.SpaceAround, // Phân bổ đều các nút
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Start/Pause Button
            // Nút Bắt đầu/Tạm dừng
//            Button(
//                onClick = {
//                    when (timerState) {
//                        PomodoroViewModel.TimerState.RUNNING -> viewModel.pauseTimer() // If running, pause
//                        else -> viewModel.startTimer() // Otherwise, start
//                    }
//                },
//                colors = ButtonDefaults.buttonColors(
//                    containerColor = when (timerState) {
//                        PomodoroViewModel.TimerState.RUNNING -> MaterialTheme.colorScheme.secondary // Different color when paused
//                        else -> MaterialTheme.colorScheme.primary // Primary color when stopped/finished
//                    }
//                ),
//                modifier = Modifier.weight(1f) // Make button take available space
//            ) {
//                Icon(
//                    imageVector = if (timerState == PomodoroViewModel.TimerState.RUNNING) Icons.Default.Star else Icons.Default.PlayArrow,
//                    contentDescription = if (timerState == PomodoroViewModel.TimerState.RUNNING) "Tạm dừng" else "Bắt đầu",
//                    tint = Color.White // White icon color
//                )
//                Spacer(modifier = Modifier.width(8.dp)) // Space between icon and text
//                Text(text = if (timerState == PomodoroViewModel.TimerState.RUNNING) "Tạm dừng" else "Bắt đầu")
//            }
//            Spacer(modifier = Modifier.width(16.dp)) // Space between buttons

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
//
//                Spacer(modifier = Modifier.width(8.dp))
//                Text(
//                    text = "Đặt lại",
//                    fontSize = 32.sp
//                )
            }

            // Reset Button
            // Nút Đặt lại
//            Button(
//                onClick = { viewModel.resetTimer() },
//                enabled = timerState != PomodoroViewModel.TimerState.STOPPED, // Only enabled if timer is not already stopped
//                modifier = Modifier.weight(1f),
//                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error) // Error color for reset
//            ) {
//                Icon(
//                    imageVector = Icons.Default.Refresh,
//                    contentDescription = "Đặt lại",
//                    tint = Color.White
//                )
//                Spacer(modifier = Modifier.width(8.dp))
//                Text(text = "Đặt lại")
//            }

            // Start / Pause
            Button(
                onClick = {
                    when (timerState) {
                        PomodoroViewModel.TimerState.RUNNING -> viewModel.pauseTimer() // If running, pause
                        else -> viewModel.startTimer() // Otherwise, start
                    }
                },
                modifier = Modifier
                    .size(96.dp) // Nút lớn hơn
                    .weight(1.5f) // Chiếm không gian lớn hơn
                    .padding(horizontal = 8.dp),
                shape = RoundedCornerShape(24.dp), // Bo tròn góc lớn hơn
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor  = buttonBackgroundColor,
                    contentColor = textColor
                )
            ) {
//                Icon(
//                    imageVector = Icons.Default.PlayArrow,
//                    contentDescription = "Play/Pause",
//                    tint = Color.White, // Màu trắng cho icon
//                    modifier = Modifier.size(48.dp)
//                )

                Icon(
                    painter = if (timerState == PomodoroViewModel.TimerState.RUNNING) painterResource(
                        R.drawable.baseline_play_arrow_24
                    ) else painterResource(R.drawable.baseline_pause_24),
                    contentDescription = if (timerState == PomodoroViewModel.TimerState.RUNNING) "Tạm dừng" else "Bắt đầu",
                    modifier = Modifier.size(48.dp),
                )

//                Spacer(modifier = Modifier.width(8.dp))
//                Text(text = if (timerState == PomodoroViewModel.TimerState.RUNNING) "Tạm dừng" else "Bắt đầu")
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
//                Spacer(modifier = Modifier.width(8.dp))
//                Text(text = "Bỏ qua phiên")
            }
        }
        // Skip Session Button
        // Nút Bỏ qua phiên
//        Button(
//            onClick = { viewModel.skipSession() },
//            modifier = Modifier.fillMaxWidth(), // Fill full width
//            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary) // Tertiary color for skip
//        ) {
//            Icon(
//                imageVector = Icons.Default.Check,
//                contentDescription = "Bỏ qua",
//                tint = Color.White
//            )
//            Spacer(modifier = Modifier.width(8.dp))
//            Text(text = "Bỏ qua phiên")
//        }
    }
}

@Preview(showBackground = true, widthDp = 320)
@Composable
fun PomodoroScreenPreview() {
    FocusFlowTheme {
        PomodoroScreen()
    }
}