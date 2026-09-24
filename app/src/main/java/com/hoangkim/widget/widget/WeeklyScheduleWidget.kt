package com.hoangkim.widget.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.hoangkim.widget.MainActivity
import java.time.LocalDate

class WeeklyScheduleWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                WidgetContent()
            }
        }
    }

    @Composable
    private fun WidgetContent() {
        val today = LocalDate.now()
        val daysFromMonday = (today.dayOfWeek.value - 1).toLong()
        val monday = today.minusDays(daysFromMonday)
        val shortDays = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ColorProvider(androidx.compose.ui.graphics.Color(0xFF14171F)))
                .cornerRadius(22.dp)
                .clickable(actionStartActivity<MainActivity>())
                .padding(14.dp)
        ) {
            Column(modifier = GlanceModifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📅 BẢNG LỊCH TUẦN",
                        style = TextStyle(
                            color = ColorProvider(androidx.compose.ui.graphics.Color(0xFF55B5FF)),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = GlanceModifier.defaultWeight())
                    Text(
                        text = "OPPO Find X9",
                        style = TextStyle(
                            color = ColorProvider(androidx.compose.ui.graphics.Color(0xFF8E9AA8)),
                            fontSize = 10.sp
                        )
                    )
                }

                Spacer(modifier = GlanceModifier.height(8.dp))

                // 7 Ngày Trong Tuần
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    (0..6).forEach { idx ->
                        val day = monday.plusDays(idx.toLong())
                        val isToday = day == today

                        Box(
                            modifier = GlanceModifier
                                .defaultWeight()
                                .background(
                                    if (isToday)
                                        ColorProvider(androidx.compose.ui.graphics.Color(0xFF2E94FF).copy(alpha = 0.25f))
                                    else
                                        ColorProvider(androidx.compose.ui.graphics.Color(0xFF1E212B))
                                )
                                .cornerRadius(8.dp)
                                .padding(vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = shortDays[idx],
                                    style = TextStyle(
                                        color = if (isToday)
                                            ColorProvider(androidx.compose.ui.graphics.Color(0xFF55B5FF))
                                        else
                                            ColorProvider(androidx.compose.ui.graphics.Color(0xFFFFFFFF).copy(alpha = 0.7f)),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = day.dayOfMonth.toString(),
                                    style = TextStyle(
                                        color = if (isToday)
                                            ColorProvider(androidx.compose.ui.graphics.Color(0xFF55B5FF))
                                        else
                                            ColorProvider(androidx.compose.ui.graphics.Color(0xFFFFFFFF)),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = GlanceModifier.height(8.dp))

                // Dòng mô tả & gợi ý chạm
                Text(
                    text = "👉 Chạm vào widget để mở lịch chi tiết và xếp lịch",
                    style = TextStyle(
                        color = ColorProvider(androidx.compose.ui.graphics.Color(0xFF8E9AA8)),
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}

class WeeklyScheduleWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = WeeklyScheduleWidget()
}
