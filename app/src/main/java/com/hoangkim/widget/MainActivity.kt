package com.hoangkim.widget

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.hoangkim.widget.ui.studio.LockScreenStudioScreen
import com.hoangkim.widget.ui.theme.WidgetAndroidTheme
import com.hoangkim.widget.ui.weekly.WeeklyGridScheduleScreen
import com.hoangkim.widget.viewmodel.StudioViewModel
import com.hoangkim.widget.viewmodel.WeeklyScheduleViewModel

class MainActivity : ComponentActivity() {
    private val weeklyViewModel: WeeklyScheduleViewModel by viewModels()
    private val studioViewModel: StudioViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            WidgetAndroidTheme {
                // Xin quyền thông báo trên Android 13+ (Tiramisu)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val permissionLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.RequestPermission(),
                        onResult = {}
                    )
                    LaunchedEffect(Unit) {
                        if (ContextCompat.checkSelfPermission(
                                this@MainActivity,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                }

                var currentScreen by remember { mutableStateOf("weekly") }

                Scaffold(
                    containerColor = Color(0xFF14171F),
                    bottomBar = {
                        NavigationBar(
                            containerColor = Color(0xFF1E212B),
                            contentColor = Color.White,
                            tonalElevation = 8.dp
                        ) {
                            NavigationBarItem(
                                selected = currentScreen == "weekly",
                                onClick = { currentScreen = "weekly" },
                                icon = {
                                    Icon(
                                        Icons.Default.CalendarMonth,
                                        contentDescription = "Bảng Lịch Tuần"
                                    )
                                },
                                label = {
                                    Text(
                                        "Bảng Lịch Tuần",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color(0xFF2E94FF),
                                    selectedTextColor = Color(0xFF2E94FF),
                                    indicatorColor = Color(0xFF2E94FF).copy(alpha = 0.15f),
                                    unselectedIconColor = Color.White.copy(alpha = 0.5f),
                                    unselectedTextColor = Color.White.copy(alpha = 0.5f)
                                )
                            )
                            NavigationBarItem(
                                selected = currentScreen == "studio",
                                onClick = { currentScreen = "studio" },
                                icon = {
                                    Icon(
                                        Icons.Default.AutoAwesome,
                                        contentDescription = "Màn Hình Khóa"
                                    )
                                },
                                label = {
                                    Text(
                                        "Màn Hình Khóa",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color(0xFF2E94FF),
                                    selectedTextColor = Color(0xFF2E94FF),
                                    indicatorColor = Color(0xFF2E94FF).copy(alpha = 0.15f),
                                    unselectedIconColor = Color.White.copy(alpha = 0.5f),
                                    unselectedTextColor = Color.White.copy(alpha = 0.5f)
                                )
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        if (currentScreen == "weekly") {
                            WeeklyGridScheduleScreen(
                                viewModel = weeklyViewModel,
                                onGoToStudio = { currentScreen = "studio" }
                            )
                        } else {
                            LockScreenStudioScreen(
                                viewModel = studioViewModel,
                                onBack = { currentScreen = "weekly" }
                            )
                        }
                    }
                }
            }
        }
    }
}
