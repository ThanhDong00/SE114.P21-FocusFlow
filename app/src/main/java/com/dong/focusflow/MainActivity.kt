package com.dong.focusflow

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dong.focusflow.data.local.entity.PomodoroSessionType
import com.dong.focusflow.ui.pomodoro.PomodoroScreen
import com.dong.focusflow.ui.settings.SettingsScreen
import com.dong.focusflow.ui.statistics.StatisticsScreen
import com.dong.focusflow.ui.theme.Blue50
import com.dong.focusflow.ui.theme.Blue500
import com.dong.focusflow.ui.theme.Blue600
import com.dong.focusflow.ui.theme.Blue900
import com.dong.focusflow.ui.theme.FocusFlowTheme
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            println("Notification permission granted")
        } else {
            println("Notification permission denied")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }

        // Lấy thông tin từ Intent nếu nó đến từ thông báo
        val initialRemainingMillis = intent.getLongExtra(PomodoroNotificationService.EXTRA_REMAINING_MILLIS, 0L)
        val initialSessionType = intent.getStringExtra(PomodoroNotificationService.EXTRA_SESSION_TYPE)
        val initialIsRunning = intent.getBooleanExtra(PomodoroNotificationService.EXTRA_IS_RUNNING, false)
        val initialAction = intent.action

        setContent {
            FocusFlowTheme {
                MainApp(
                    initialRemainingMillis = initialRemainingMillis,
                    initialSessionType = initialSessionType?.let { PomodoroSessionType.valueOf(it) },
                    initialIsRunning = initialIsRunning,
                    initialAction = initialAction
                )
            }
        }
    }
}

@Composable
fun MainApp(
    initialRemainingMillis: Long = 0L,
    initialSessionType: PomodoroSessionType? = null,
    initialIsRunning: Boolean = false,
    initialAction: String? = null
) {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Blue50
            ) {
                BottomNavItem.values().forEach { item ->
                    val selected = currentRoute == item.route
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            if (currentRoute != item.route) {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title
                            )
                        },
                        label = { Text(item.title) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Blue900,
                            unselectedIconColor = Blue500,
                            selectedTextColor = Blue900,
                            unselectedTextColor = Blue500,
                            indicatorColor = Blue600
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.POMODORO.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(BottomNavItem.POMODORO.route) {
                PomodoroScreen(
                    initialRemainingMillis = initialRemainingMillis,
                    initialSessionType = initialSessionType,
                    initialIsRunning = initialIsRunning,
                    initialAction = initialAction
                )
            }
            composable(BottomNavItem.STATISTICS.route) {
                StatisticsScreen()
            }
            composable(BottomNavItem.SETTINGS.route) {
                SettingsScreen()
            }
        }
    }
}

/**
 * Mỗi mục có tiêu đề, biểu tượng và một route duy nhất.
 */
enum class BottomNavItem(val title: String, val icon: ImageVector, val route: String) {
    POMODORO("Pomodoro", Icons.Default.Home, "pomodoro_screen"),
    STATISTICS("Thống kê", Icons.Default.Star, "statistics_screen"),
    SETTINGS("Cài đặt", Icons.Default.Settings, "settings_screen")
}

/**
 * Hàm Preview cho Composable MainApp.
 */
@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    FocusFlowTheme {
        MainApp()
    }
}

