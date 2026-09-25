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
import androidx.compose.runtime.*
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
