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
    // Khởi tạo ActivityResultLauncher để yêu cầu quyền thông báo
    // Initialize ActivityResultLauncher to request notification permission
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Quyền được cấp, có thể hiển thị thông báo
            // Permission granted, can show notifications
            println("Notification permission granted")
        } else {
            // Quyền bị từ chối, không thể hiển thị thông báo
            // Permission denied, cannot show notifications
            println("Notification permission denied")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Yêu cầu quyền POST_NOTIFICATIONS từ Android 13 (API 33) trở lên
        // Request POST_NOTIFICATIONS permission from Android 13 (API 33) and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Quyền đã được cấp
                    // Permission already granted
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    // Giải thích lý do cần quyền cho người dùng (có thể hiển thị dialog)
                    // Explain why the permission is needed (can show a dialog)
                    // Hiện tại, chúng ta chỉ gọi yêu cầu quyền trực tiếp
                    // For now, we just call the permission request directly
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                else -> {
                    // Yêu cầu quyền lần đầu
                    // Request permission for the first time
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }

        setContent {
            FocusFlowTheme { // Apply the custom theme
                MainApp() // Call the main Composable function
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class) // Opt-in for experimental Material 3 APIs like TopAppBar
@Composable
fun MainApp() {
    val navController = rememberNavController() // Create and remember a NavController
    val currentBackStackEntry by navController.currentBackStackEntryAsState() // Observe current navigation state
    val currentRoute = currentBackStackEntry?.destination?.route // Get the current route

    Scaffold(
        bottomBar = {
            // Bottom navigation bar to switch between screens
            NavigationBar(
                containerColor = Blue50
            ) {
                // Iterate through defined navigation items
                BottomNavItem.values().forEach { item ->
                    val selected = currentRoute == item.route // Check if current item is selected
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            if (currentRoute != item.route) { // Navigate only if not already on the selected route
                                navController.navigate(item.route) {
                                    // Pop up to the start destination of the graph to
                                    // avoid building up a large stack of destinations
                                    // on the back stack as users select items
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true // Save state of popped destinations
                                    }
                                    // Avoid multiple copies of the same destination when
                                    // reselecting the same item
                                    launchSingleTop = true
                                    // Restore state when reselecting a previously selected item
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = item.icon, // Icon for the navigation item
                                contentDescription = item.title // Content description for accessibility
                            )
                        },
                        label = { Text(item.title) }, // Text label for the navigation item
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Blue900, // Ví dụ: đổi màu icon khi được chọn
                            unselectedIconColor = Blue500, // Ví dụ: đổi màu icon khi không được chọn
                            selectedTextColor = Blue900, // Ví dụ: đổi màu chữ khi được chọn
                            unselectedTextColor = Blue500, // Ví dụ: đổi màu chữ khi không được chọn
                            indicatorColor = Blue600 // Ví dụ: đổi màu chỉ báo (nếu có)
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        // NavHost defines the navigation graph
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.POMODORO.route, // Set Pomodoro as the initial screen
            modifier = Modifier.padding(paddingValues) // Apply padding from Scaffold
        ) {
            // Define composable destinations for each route
            composable(BottomNavItem.POMODORO.route) {
                PomodoroScreen()
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
 * Sealed class để định nghĩa các mục điều hướng dưới cùng.
 * Mỗi mục có tiêu đề, biểu tượng và một route duy nhất.
 */
enum class BottomNavItem(val title: String, val icon: ImageVector, val route: String) {
    POMODORO("Pomodoro", Icons.Default.Home, "pomodoro_screen"), // Route for Pomodoro screen
    STATISTICS("Thống kê", Icons.Default.Star, "statistics_screen"), // Route for Statistics screen
    SETTINGS("Cài đặt", Icons.Default.Settings, "settings_screen") // Route for Settings screen
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

