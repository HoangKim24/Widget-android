package com.hoangkim.widget

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.hoangkim.widget.repository.EventRepository
import com.hoangkim.widget.ui.studio.LockScreenStudioScreen
import com.hoangkim.widget.ui.theme.WidgetAndroidTheme
import com.hoangkim.widget.ui.weekly.WeeklyGridScheduleScreen

class MainActivity : ComponentActivity() {
    private lateinit var repository: EventRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        repository = EventRepository(applicationContext)

        setContent {
            WidgetAndroidTheme {
                var currentScreen by remember { mutableStateOf("weekly") }

                if (currentScreen == "weekly") {
                    WeeklyGridScheduleScreen(
                        repository = repository,
                        onGoToStudio = { currentScreen = "studio" }
                    )
                } else {
                    LockScreenStudioScreen(
                        repository = repository,
                        onBack = { currentScreen = "weekly" }
                    )
                }
            }
        }
    }
}
