package com.hoangkim.widget.ui.weekly

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hoangkim.widget.model.CalendarEvent
import com.hoangkim.widget.model.DayOfWeekEnum
import com.hoangkim.widget.ui.theme.AccentGold
import com.hoangkim.widget.ui.theme.CardDark
import com.hoangkim.widget.ui.theme.CardStroke
import com.hoangkim.widget.ui.theme.LiveIndicatorRed
import com.hoangkim.widget.ui.theme.PrimaryAccent
import com.hoangkim.widget.ui.theme.SurfaceDark
import com.hoangkim.widget.ui.theme.TextPrimary
import com.hoangkim.widget.ui.theme.TextSecondary
import java.util.Calendar

@Composable
fun WeeklyGridScheduleScreen(
    onAddEventClick: () -> Unit = {}
) {
    val sampleEvents = remember {
        mutableStateOf(
            listOf(
                CalendarEvent(
                    title = "Toán Cao Cấp",
                    location = "Phòng A.201",
                    dayOfWeek = DayOfWeekEnum.MONDAY,
                    startHour = 7,
                    startMinute = 30,
                    endHour = 9,
                    endMinute = 30,
                    colorHex = "#1E88E5"
                ),
                CalendarEvent(
                    title = "Lập Trình Android",
                    location = "Phòng Lab 3",
                    dayOfWeek = DayOfWeekEnum.WEDNESDAY,
                    startHour = 13,
                    startMinute = 0,
                    endHour = 16,
                    endMinute = 0,
                    colorHex = "#FFA726"
                ),
                CalendarEvent(
                    title = "Họp Nhóm Đồ Án",
                    location = "Online Meet",
                    dayOfWeek = DayOfWeekEnum.FRIDAY,
                    startHour = 19,
                    startMinute = 0,
                    endHour = 20,
                    endMinute = 30,
                    colorHex = "#42A5F5"
                )
            )
        )
    }

    val calendar = Calendar.getInstance()
    val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
    val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
    val currentMinute = calendar.get(Calendar.MINUTE)

    // Chuyển Calendar.DAY_OF_WEEK (Sun=1, Mon=2) sang DayOfWeekEnum
    val todayEnum = when (currentDayOfWeek) {
        Calendar.MONDAY -> DayOfWeekEnum.MONDAY
        Calendar.TUESDAY -> DayOfWeekEnum.TUESDAY
        Calendar.WEDNESDAY -> DayOfWeekEnum.WEDNESDAY
        Calendar.THURSDAY -> DayOfWeekEnum.THURSDAY
        Calendar.FRIDAY -> DayOfWeekEnum.FRIDAY
        Calendar.SATURDAY -> DayOfWeekEnum.SATURDAY
        else -> DayOfWeekEnum.SUNDAY
    }

    Scaffold(
        containerColor = SurfaceDark,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddEventClick,
                containerColor = PrimaryAccent,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Thêm lịch")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Lịch Tuần Này",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "OPPO Find X9 • 7 Ngày Trực Quan",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }
            }

            // Thanh thứ trong tuần (Mon -> Sun)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                DayOfWeekEnum.entries.forEach { day ->
                    val isToday = day == todayEnum
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 2.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isToday) PrimaryAccent else CardDark)
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = day.shortName,
                                fontSize = 12.sp,
                                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                                color = if (isToday) Color.White else TextSecondary
                            )
                        }
                    }
                }
            }

            // Danh sách lịch trình có Vạch Real-time Indicator
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(sampleEvents.value.size) { index ->
                    val event = sampleEvents.value[index]
                    val isLiveNow = event.isHappeningNow(todayEnum, currentHour, currentMinute)

                    EventCard(event = event, isLiveNow = isLiveNow)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun EventCard(event: CalendarEvent, isLiveNow: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardDark)
            .border(
                width = if (isLiveNow) 2.dp else 1.dp,
                color = if (isLiveNow) LiveIndicatorRed.copy(alpha = alphaAnim) else CardStroke,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Cột ngày & giờ
            Column(modifier = Modifier.width(90.dp)) {
                Text(
                    text = event.dayOfWeek.shortName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentGold
                )
                Text(
                    text = event.timeRangeFormatted,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }

            // Đường gạch đứng chia
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(36.dp)
                    .clip(CircleShape)
                    .background(Color(android.graphics.Color.parseColor(event.colorHex)))
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Nội dung môn học
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                if (event.location.isNotEmpty()) {
                    Text(
                        text = event.location,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }

            // Huy hiệu Realtime "LIVE" nếu đang trong giờ
            if (isLiveNow) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(LiveIndicatorRed)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "LIVE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            } else if (event.enableAlarm) {
                Icon(
                    imageVector = Icons.Default.Alarm,
                    contentDescription = "Có báo thức",
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
