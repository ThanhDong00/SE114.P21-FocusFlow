package com.dong.focusflow.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dong.focusflow.ui.theme.Blue50
import com.dong.focusflow.ui.theme.Blue900
import com.dong.focusflow.ui.theme.FocusFlowTheme
import kotlinx.coroutines.launch

/**
 * Composable function for the Settings screen.
 * Allows users to configure focus time, short break time, and long break time.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val focusTime by viewModel.focusTime.collectAsState()
    val shortBreakTime by viewModel.shortBreakTime.collectAsState()
    val longBreakTime by viewModel.longBreakTime.collectAsState()
    val shortBreaksBeforeLongBreak by viewModel.shortBreaksBeforeLongBreak.collectAsState()
    val userMessage by viewModel.userMessage.collectAsState()

    var focusTimeInput by remember { mutableStateOf(focusTime.toString()) }
    var shortBreakTimeInput by remember { mutableStateOf(shortBreakTime.toString()) }
    var longBreakTimeInput by remember { mutableStateOf(longBreakTime.toString()) }
    var shortBreaksBeforeLongBreakInput by remember { mutableStateOf(shortBreaksBeforeLongBreak.toString()) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Update input fields when settings change
    LaunchedEffect(focusTime) { focusTimeInput = focusTime.toString() }
    LaunchedEffect(shortBreakTime) { shortBreakTimeInput = shortBreakTime.toString() }
    LaunchedEffect(longBreakTime) { longBreakTimeInput = longBreakTime.toString() }
    LaunchedEffect(shortBreaksBeforeLongBreak) {
        shortBreaksBeforeLongBreakInput = shortBreaksBeforeLongBreak.toString()
    }

    // Handle user messages
    LaunchedEffect(userMessage) {
        userMessage?.let { message ->
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    message = message,
                    duration = SnackbarDuration.Short
                )
                viewModel.clearUserMessage()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Pomodoro Settings",
                        color = Blue900,
                        fontWeight = FontWeight.Bold)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Blue50
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        val scrollState = rememberScrollState()
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Blue50)
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            SettingsTextField(
                value = focusTimeInput,
                onValueChange = { focusTimeInput = it.filter { char -> char.isDigit() } },
                label = "Focus Time (minutes)"
            )

            Spacer(modifier = Modifier.height(16.dp))

            SettingsTextField(
                value = shortBreakTimeInput,
                onValueChange = { shortBreakTimeInput = it.filter { char -> char.isDigit() } },
                label = "Short Break Time (minutes)"
            )

            Spacer(modifier = Modifier.height(16.dp))

            SettingsTextField(
                value = longBreakTimeInput,
                onValueChange = { longBreakTimeInput = it.filter { char -> char.isDigit() } },
                label = "Long Break Time (minutes)"
            )

            Spacer(modifier = Modifier.height(16.dp))

            SettingsTextField(
                value = shortBreaksBeforeLongBreakInput,
                onValueChange = {
                    shortBreaksBeforeLongBreakInput = it.filter { char -> char.isDigit() }
                },
                label = "Work Sessions Before Long Break"
            )

            Spacer(modifier = Modifier.height(24.dp))

            SaveButton(
                onClick = {
                    saveAllSettings(
                        viewModel = viewModel,
                        focusTime = focusTimeInput,
                        shortBreakTime = shortBreakTimeInput,
                        longBreakTime = longBreakTimeInput,
                        shortBreaksBeforeLongBreak = shortBreaksBeforeLongBreakInput
                    )
                }
            )
        }
    }
}

@Composable
private fun SettingsTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Blue900,
            focusedLabelColor = Blue900,
            cursorColor = Blue900
        )
    )
}

@Composable
private fun SaveButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = Blue900,
            contentColor = Blue50
        )
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = "Save Settings"
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text("Lưu cài đặt")
    }
}

private fun saveAllSettings(
    viewModel: SettingsViewModel,
    focusTime: String,
    shortBreakTime: String,
    longBreakTime: String,
    shortBreaksBeforeLongBreak: String
) {
    focusTime.toIntOrNull()?.let { viewModel.saveFocusTime(it) }
    shortBreakTime.toIntOrNull()?.let { viewModel.saveShortBreakTime(it) }
    longBreakTime.toIntOrNull()?.let { viewModel.saveLongBreakTime(it) }
    shortBreaksBeforeLongBreak.toIntOrNull()?.let { viewModel.saveShortBreaksBeforeLongBreak(it) }
}

/**
 * Preview function for SettingsScreen Composable.
 */
@Preview(showBackground = true, widthDp = 320)
@Composable
fun SettingsScreenPreview() {
    FocusFlowTheme {
        SettingsScreen()
    }
}