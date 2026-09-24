package com.hoangkim.widget

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.hoangkim.widget.ui.theme.WidgetAndroidTheme
import com.hoangkim.widget.ui.weekly.WeeklyGridScheduleScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WidgetAndroidTheme {
                WeeklyGridScheduleScreen()
            }
        }
    }
}
