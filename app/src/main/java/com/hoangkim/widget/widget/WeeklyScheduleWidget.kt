package com.hoangkim.widget.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
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
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.hoangkim.widget.MainActivity
import com.hoangkim.widget.model.CalendarEvent
import com.hoangkim.widget.repository.EventRepository
import java.time.LocalDate

/**
 * Tiện ích màn hình chính (Widget Glance) cho ColorOS (OPPO Find X9).
 * Hiển thị dải 7 ngày trong tuần và danh sách lịch trình hôm nay theo thời gian thực.
 */
class WeeklyScheduleWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = EventRepository(context)
        val allEvents = repository.events.value

        provideContent {
            GlanceTheme {
                WidgetContent(allEvents)
            }
        }
    }

    @Composable
    private fun WidgetContent(events: List<CalendarEvent>) {
        val today = LocalDate.now()
        val daysFromMonday = (today.dayOfWeek.value - 1).toLong()
        val monday = today.minusDays(daysFromMonday)
        val shortDays = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")

        val todayEvents = events.filter { it.occurs(today) }.sortedBy { it.startDate }

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ColorProvider(Color(0xFF14171F)))
                .cornerRadius(22.dp)
                .clickable(actionStartActivity<MainActivity>())
                .padding(12.dp)
        ) {
            Column(modifier = GlanceModifier.fillMaxSize()) {
                // MARK: - 1. HEADER WIDGET
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📅 LỊCH TUẦN & HÔM NAY",
                        style = TextStyle(
                            color = ColorProvider(Color(0xFF55B5FF)),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = GlanceModifier.defaultWeight())
                    Text(
                        text = "Hôm nay: ${today.dayOfMonth}/${today.monthValue} (${todayEvents.size} việc)",
                        style = TextStyle(
                            color = ColorProvider(Color(0xFF8E9AA8)),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Spacer(modifier = GlanceModifier.height(6.dp))

                // MARK: - 2. DẢI 7 NGÀY TRONG TUẦN
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    (0..6).forEach { idx ->
                        val day = monday.plusDays(idx.toLong())
                        val isToday = day == today
                        val dayEventCount = events.count { it.occurs(day) }

                        Box(
                            modifier = GlanceModifier
                                .defaultWeight()
                                .background(
                                    if (isToday)
                                        ColorProvider(Color(0xFF2E94FF).copy(alpha = 0.25f))
                                    else
                                        ColorProvider(Color(0xFF1E212B))
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
                                            ColorProvider(Color(0xFF55B5FF))
                                        else
                                            ColorProvider(Color.White.copy(alpha = 0.7f)),
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = day.dayOfMonth.toString(),
                                    style = TextStyle(
                                        color = if (isToday)
                                            ColorProvider(Color(0xFF55B5FF))
                                        else
                                            ColorProvider(Color.White),
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                if (dayEventCount > 0) {
                                    Text(
                                        text = "•".repeat(minOf(dayEventCount, 3)),
                                        style = TextStyle(
                                            color = if (isToday)
                                                ColorProvider(Color(0xFF55B5FF))
                                            else
                                                ColorProvider(Color(0xFF47C5E2)),
                                            fontSize = 7.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = GlanceModifier.height(8.dp))

                // MARK: - 3. DANH SÁCH LỊCH TRÌNH HÔM NAY (Hiển thị trực tiếp)
                if (todayEvents.isNotEmpty()) {
                    todayEvents.take(3).forEach { ev ->
                        val isLive = ev.isHappeningNow()
                        Row(
                            modifier = GlanceModifier
                                .fillMaxWidth()
                                .background(ColorProvider(Color(0xFF1E212B)))
                                .cornerRadius(6.dp)
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Vạch màu phân loại
                            Box(
                                modifier = GlanceModifier
                                    .width(3.dp)
                                    .height(20.dp)
                                    .cornerRadius(2.dp)
                                    .background(ColorProvider(ev.category.composeColor))
                            ) {}

                            Spacer(modifier = GlanceModifier.width(6.dp))

                            // Giờ diễn ra
                            Text(
                                text = ev.timeRangeFormatted,
                                style = TextStyle(
                                    color = if (isLive)
                                        ColorProvider(Color(0xFFFFA726))
                                    else
                                        ColorProvider(Color.White.copy(alpha = 0.85f)),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )

                            Spacer(modifier = GlanceModifier.width(6.dp))

                            // Tên việc & địa điểm
                            val desc = if (ev.location.isNotEmpty()) "${ev.title} (${ev.location})" else ev.title
                            Text(
                                text = desc,
                                style = TextStyle(
                                    color = ColorProvider(Color.White),
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                maxLines = 1
                            )

                            if (isLive) {
                                Spacer(modifier = GlanceModifier.defaultWeight())
                                Box(
                                    modifier = GlanceModifier
                                        .background(ColorProvider(Color(0xFFFF3B30)))
                                        .cornerRadius(4.dp)
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "LIVE",
                                        style = TextStyle(
                                            color = ColorProvider(Color.White),
                                            fontSize = 7.5.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }
                        Spacer(modifier = GlanceModifier.height(4.dp))
                    }

                    if (todayEvents.size > 3) {
                        Text(
                            text = "👉 Còn +${todayEvents.size - 3} sự kiện khác • Chạm để mở app",
                            style = TextStyle(
                                color = ColorProvider(Color(0xFF8E9AA8)),
                                fontSize = 9.5.sp
                            )
                        )
                    }
                } else {
                    Box(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .background(ColorProvider(Color(0xFF1E212B).copy(alpha = 0.6f)))
                            .cornerRadius(8.dp)
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "✨ Hôm nay không có lịch trình • Thảnh thơi nghỉ ngơi!",
                            style = TextStyle(
                                color = ColorProvider(Color(0xFF8E9AA8)),
                                fontSize = 10.5.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

class WeeklyScheduleWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = WeeklyScheduleWidget()
}
