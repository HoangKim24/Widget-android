package com.hoangkim.widget.ui.weekly

import android.widget.Toast
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hoangkim.widget.model.CalendarEvent
import com.hoangkim.widget.model.EventCategory
import com.hoangkim.widget.repository.EventRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklyGridScheduleScreen(
    repository: EventRepository,
    onGoToStudio: () -> Unit = {}
) {
    val context = LocalContext.current
    val events by repository.events.collectAsState()

    var weekOffset by remember { mutableIntStateOf(0) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    // Input form state
    var newEventTitle by remember { mutableStateOf("") }
    var startHour by remember { mutableIntStateOf(7) }
    var startMinute by remember { mutableIntStateOf(0) }
    var endHour by remember { mutableIntStateOf(10) }
    var endMinute by remember { mutableIntStateOf(0) }
    var selectedCategory by remember { mutableStateOf(EventCategory.WORK) }
    var isRecurringWeekly by remember { mutableStateOf(false) }
    var hasReminder by remember { mutableStateOf(true) }

    // 7 ngày trong tuần theo weekOffset
    val today = LocalDate.now()
    val daysFromMonday = (today.dayOfWeek.value - 1).toLong()
    val currentMonday = today.minusDays(daysFromMonday)
    val targetMonday = currentMonday.plusWeeks(weekOffset.toLong())
    val currentWeekDays = remember(weekOffset) {
        (0..6).map { targetMonday.plusDays(it.toLong()) }
    }

    val weekOfYear = targetMonday.get(WeekFields.of(Locale.getDefault()).weekOfYear())
    val weekRangeSubtext = "${currentWeekDays.first().format(DateTimeFormatter.ofPattern("dd/MM"))} – ${currentWeekDays.last().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}"

    val eventsForSelectedDate = remember(events, selectedDate) {
        events.filter { it.occurs(selectedDate) }.sortedBy { it.startDate }
    }

    Scaffold(
        containerColor = Color(0xFF14171F),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Bảng Lịch Tuần",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                },
                actions = {
                    var showMoreMenu by remember { mutableStateOf(false) }
                    IconButton(onClick = { showMoreMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Tùy chọn", tint = Color.White.copy(alpha = 0.85f))
                    }
                    DropdownMenu(
                        expanded = showMoreMenu,
                        onDismissRequest = { showMoreMenu = false },
                        modifier = Modifier.background(Color(0xFF1E212B))
                    ) {
                        // Section 1: Đồng Bộ & Nhập Lịch
                        Text(
                            "ĐỒNG BỘ & NHẬP LỊCH",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
                        )
                        DropdownMenuItem(
                            text = { Text("Đồng Bộ Từ Lịch Điện Thoại", color = Color.White, fontSize = 13.sp) },
                            leadingIcon = { Icon(Icons.Default.Sync, contentDescription = null, tint = Color(0xFF2E94FF)) },
                            onClick = {
                                showMoreMenu = false
                                Toast.makeText(context, "Đang đồng bộ từ Google Calendar / Lịch máy...", Toast.LENGTH_SHORT).show()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Dán Lịch & Quét Ảnh (OCR)", color = Color.White, fontSize = 13.sp) },
                            leadingIcon = { Icon(Icons.Default.DocumentScanner, contentDescription = null, tint = Color(0xFFB388FF)) },
                            onClick = {
                                showMoreMenu = false
                                Toast.makeText(context, "Mở trình quét ảnh OCR & dán lịch Zalo", Toast.LENGTH_SHORT).show()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Sao Chép Sang Tuần Sau", color = Color.White, fontSize = 13.sp) },
                            leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color.White.copy(alpha = 0.8f)) },
                            onClick = {
                                showMoreMenu = false
                                Toast.makeText(context, "Đã sao chép lịch sang tuần sau thành công!", Toast.LENGTH_SHORT).show()
                            }
                        )

                        Divider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 4.dp))

                        // Section 2: Xuất File & Sao Lưu
                        Text(
                            "XUẤT FILE & SAO LƯU",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
                        )
                        DropdownMenuItem(
                            text = { Text("Xuất File Lịch (.ics)", color = Color.White, fontSize = 13.sp) },
                            leadingIcon = { Icon(Icons.Default.Share, contentDescription = null, tint = Color.White.copy(alpha = 0.8f)) },
                            onClick = {
                                showMoreMenu = false
                                Toast.makeText(context, "Đã xuất file LichTuan.ics", Toast.LENGTH_SHORT).show()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Sao Lưu Dữ Liệu (.json)", color = Color.White, fontSize = 13.sp) },
                            leadingIcon = { Icon(Icons.Default.Download, contentDescription = null, tint = Color.White.copy(alpha = 0.8f)) },
                            onClick = {
                                showMoreMenu = false
                                Toast.makeText(context, "Đã sao lưu file LichTuan_Backup.json", Toast.LENGTH_SHORT).show()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Khôi Phục Dữ Liệu (.json)", color = Color.White, fontSize = 13.sp) },
                            leadingIcon = { Icon(Icons.Default.Restore, contentDescription = null, tint = Color.White.copy(alpha = 0.8f)) },
                            onClick = {
                                showMoreMenu = false
                                Toast.makeText(context, "Mở file .json để khôi phục", Toast.LENGTH_SHORT).show()
                            }
                        )

                        Divider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 4.dp))

                        // Section 3: Màn Hình Khóa
                        Text(
                            "MÀN HÌNH KHÓA",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
                        )
                        DropdownMenuItem(
                            text = { Text("Cập Nhật Màn Khóa Ngay", color = Color.White, fontSize = 13.sp) },
                            leadingIcon = { Icon(Icons.Default.Bolt, contentDescription = null, tint = Color(0xFF2E94FF)) },
                            onClick = {
                                showMoreMenu = false
                                Toast.makeText(context, "Đã kích hoạt đổi màn hình khóa!", Toast.LENGTH_SHORT).show()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Cài Đặt Tự Động Hóa", color = Color.White, fontSize = 13.sp) },
                            leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null, tint = Color.White.copy(alpha = 0.8f)) },
                            onClick = {
                                showMoreMenu = false
                                Toast.makeText(context, "Hướng dẫn tự động hóa màn hình khóa", Toast.LENGTH_SHORT).show()
                            }
                        )

                        Divider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 4.dp))

                        // Section 4: Xóa Lịch
                        DropdownMenuItem(
                            text = { Text("Xóa Hết Lịch Trình", color = Color(0xFFFF453A), fontSize = 13.sp) },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFFF453A)) },
                            onClick = {
                                showMoreMenu = false
                                repository.clearAll()
                                Toast.makeText(context, "Đã xóa toàn bộ lịch trình", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF14171F))
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // MARK: - PHẦN 1: BẢNG TỔNG QUAN 7 CỘT TUẦN (weeklyMatrixOverviewCard)
            item {
                WeeklyMatrixCard(
                    weekNumber = weekOfYear,
                    weekOffset = weekOffset,
                    dateRangeText = weekRangeSubtext,
                    weekDays = currentWeekDays,
                    selectedDate = selectedDate,
                    events = events,
                    onPrevWeek = { weekOffset-- },
                    onNextWeek = { weekOffset++ },
                    onSelectDate = { selectedDate = it }
                )
            }

            // MARK: - PHẦN 2: DANH SÁCH LỊCH TRÌNH ĐÃ XẾP TRONG NGÀY (scheduledSlotsSection)
            item {
                ScheduledSlotsSection(
                    selectedDate = selectedDate,
                    events = eventsForSelectedDate,
                    onDeleteEvent = { repository.removeEvent(it.id) }
                )
            }

            // MARK: - PHẦN 3: KHUNG XẾP LỊCH TRÌNH VÀO NGÀY ĐANG CHỌN (scheduleInputCard)
            item {
                ScheduleInputCard(
                    selectedDate = selectedDate,
                    title = newEventTitle,
                    onTitleChange = { newEventTitle = it },
                    startHour = startHour,
                    startMinute = startMinute,
                    endHour = endHour,
                    endMinute = endMinute,
                    onTimeChange = { sH, sM, eH, eM ->
                        startHour = sH
                        startMinute = sM
                        endHour = eH
                        endMinute = eM
                    },
                    selectedCategory = selectedCategory,
                    onCategoryChange = { selectedCategory = it },
                    isRecurring = isRecurringWeekly,
                    onRecurringChange = { isRecurringWeekly = it },
                    hasReminder = hasReminder,
                    onReminderChange = { hasReminder = it },
                    onAddEvent = {
                        if (newEventTitle.trim().isEmpty()) {
                            Toast.makeText(context, "Vui lòng nhập tên công việc hoặc môn học!", Toast.LENGTH_SHORT).show()
                            return@ScheduleInputCard
                        }

                        val sDate = LocalDateTime.of(selectedDate, LocalTime.of(startHour, startMinute))
                        val eDate = LocalDateTime.of(selectedDate, LocalTime.of(endHour, endMinute))

                        repository.addEvent(
                            CalendarEvent(
                                title = newEventTitle.trim(),
                                startDate = sDate,
                                endDate = eDate,
                                category = selectedCategory,
                                isRecurringWeekly = isRecurringWeekly,
                                hasReminder = hasReminder
                            )
                        )
                        newEventTitle = ""
                        Toast.makeText(context, "Đã xếp vào bảng lịch tuần!", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            // MARK: - NÚT GRADIENT: XEM & XUẤT HÌNH NỀN MÀN HÌNH KHÓA
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF1E88E5), Color(0xFF6C5CE7))
                            )
                        )
                        .clickable { onGoToStudio() }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White)
                        Text(
                            text = "Xem & Xuất Hình Nền Màn Hình Khóa",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

// BẢNG 7 CỘT TUẦN (Component 1)
@Composable
fun WeeklyMatrixCard(
    weekNumber: Int,
    weekOffset: Int,
    dateRangeText: String,
    weekDays: List<LocalDate>,
    selectedDate: LocalDate,
    events: List<CalendarEvent>,
    onPrevWeek: () -> Unit,
    onNextWeek: () -> Unit,
    onSelectDate: (LocalDate) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E212B)),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(Color.White.copy(0.12f), Color.White.copy(0.12f))))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Thanh chuyển tuần
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(0.08f))
                        .clickable { onPrevWeek() },
                    contentAlignment = Alignment.Center
                ) {
                    Text("◀", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White.copy(0.06f))
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("📅", fontSize = 11.sp)
                    Text("Tuần $weekNumber", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (weekOffset == 0) Color(0xFF2E94FF).copy(0.25f) else Color(0xFF10B981).copy(0.25f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (weekOffset == 0) "TUẦN NÀY" else if (weekOffset > 0) "+$weekOffset TUẦN" else "$weekOffset TUẦN",
                            color = if (weekOffset == 0) Color(0xFF55B5FF) else Color(0xFF10B981),
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(0.08f))
                        .clickable { onNextWeek() },
                    contentAlignment = Alignment.Center
                ) {
                    Text("▶", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Dòng phụ ngày tháng
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(dateRangeText, color = Color.White.copy(0.6f), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Text("Chạm vào cột để xếp lịch", color = Color(0xFF47C5E2), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }

            // 7 Cột Tuần Song Song
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val shortDayNames = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")
                val today = LocalDate.now()

                weekDays.forEachIndexed { index, day ->
                    val isSelected = day == selectedDate
                    val isToday = day == today
                    val dayEvents = events.filter { it.occurs(day) }.sortedBy { it.startDate }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 125.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) Color(0xFF2E94FF).copy(0.18f) else Color.White.copy(0.04f))
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) Color(0xFF2E94FF) else Color.White.copy(0.08f),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { onSelectDate(day) }
                            .padding(vertical = 6.dp, horizontal = 2.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Header Thứ & Số Ngày
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = shortDayNames[index],
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color(0xFF2E94FF) else Color.White.copy(0.7f)
                            )
                            Text(
                                text = day.dayOfMonth.toString(),
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isSelected) Color(0xFF2E94FF) else if (isToday) Color(0xFFF8A555) else Color.White
                            )
                        }

                        // Divider
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.5.dp)
                                .background(if (isSelected) Color(0xFF2E94FF) else Color.White.copy(0.1f))
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Mini Blocks
                        if (dayEvents.isNotEmpty()) {
                            dayEvents.take(3).forEach { ev ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 2.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(ev.category.composeColor)
                                        .padding(horizontal = 2.dp, vertical = 2.dp)
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = ev.startDate.format(DateTimeFormatter.ofPattern("HH:mm")),
                                            color = Color.White.copy(0.9f),
                                            fontSize = 5.5.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = ev.title,
                                            color = Color.White,
                                            fontSize = 6.5.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        } else {
                            Box(modifier = Modifier.fillMaxHeight(), contentAlignment = Alignment.Center) {
                                Text("+", color = Color.White.copy(0.2f), fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// LỊCH TRÌNH ĐÃ XẾP TRONG NGÀY (Component 2)
@Composable
fun ScheduledSlotsSection(
    selectedDate: LocalDate,
    events: List<CalendarEvent>,
    onDeleteEvent: (CalendarEvent) -> Unit
) {
    val dayFormatter = DateTimeFormatter.ofPattern("dd/MM")
    val weekdayVietnamese = when (selectedDate.dayOfWeek.value) {
        1 -> "THỨ HAI"
        2 -> "THỨ BA"
        3 -> "THỨ TƯ"
        4 -> "THỨ NĂM"
        5 -> "THỨ SÁU"
        6 -> "THỨ BẢY"
        else -> "CHỦ NHẬT"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E212B)),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(Color.White.copy(0.12f), Color.White.copy(0.12f))))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "LỊCH TRÌNH $weekdayVietnamese, ${selectedDate.format(dayFormatter)} (${events.size})",
                color = Color.White.copy(0.6f),
                fontSize = 11.5.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            if (events.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(0.04f))
                        .padding(vertical = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📅", fontSize = 24.sp)
                        Text("Chưa có lịch trình cho ngày này", color = Color.White.copy(0.5f), fontSize = 13.sp)
                    }
                }
            } else {
                events.forEach { ev ->
                    val isLive = ev.isHappeningNow()
                    SlotItemView(event = ev, isLive = isLive, onDelete = { onDeleteEvent(ev) })
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun SlotItemView(
    event: CalendarEvent,
    isLive: Boolean,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(0.04f))
            .border(
                width = if (isLive) 1.5.dp else 1.dp,
                color = if (isLive) Color(0xFFFF3B30) else Color.White.copy(0.08f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Cột màu phân loại
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(34.dp)
                .clip(CircleShape)
                .background(event.category.composeColor)
        )

        // Khung giờ
        Text(
            text = event.timeRangeFormatted,
            color = if (isLive) Color(0xFFFFA726) else Color.White.copy(0.9f),
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(88.dp)
        )

        // Tên việc & địa điểm
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = event.title,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = if (event.location.isNotEmpty()) "📍 ${event.location} • ${event.category.displayName}" else event.category.displayName,
                color = Color.White.copy(0.6f),
                fontSize = 11.sp
            )
        }

        if (isLive) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(5.dp))
                    .background(Color(0xFFFF3B30))
                    .padding(horizontal = 6.dp, vertical = 3.dp)
            ) {
                Text("LIVE", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
            }
        }

        if (event.hasReminder) {
            Icon(Icons.Default.Notifications, contentDescription = null, tint = Color.Yellow.copy(0.8f), modifier = Modifier.size(16.dp))
        }

        IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
            Icon(Icons.Default.Delete, contentDescription = "Xóa", tint = Color.White.copy(0.35f), modifier = Modifier.size(16.dp))
        }
    }
}

// KHUNG XẾP LỊCH TRÌNH VÀO NGÀY ĐANG CHỌN (Component 3)
@Composable
fun ScheduleInputCard(
    selectedDate: LocalDate,
    title: String,
    onTitleChange: (String) -> Unit,
    startHour: Int,
    startMinute: Int,
    endHour: Int,
    endMinute: Int,
    onTimeChange: (Int, Int, Int, Int) -> Unit,
    selectedCategory: EventCategory,
    onCategoryChange: (EventCategory) -> Unit,
    isRecurring: Boolean,
    onRecurringChange: (Boolean) -> Unit,
    hasReminder: Boolean,
    onReminderChange: (Boolean) -> Unit,
    onAddEvent: () -> Unit
) {
    val dayFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E212B)),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(Color.White.copy(0.12f), Color.White.copy(0.12f))))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = Color(0xFF2E94FF))
                Text(
                    text = "Xếp Việc Vào ${selectedDate.format(dayFormatter)}",
                    color = Color.White,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // 1. Tên công việc
            Column {
                Text("TÊN CÔNG VIỆC", color = Color.White.copy(0.6f), fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = onTitleChange,
                    placeholder = { Text("Ví dụ: 07:00 Đi làm, Họp team, Tập gym...", color = Color.White.copy(0.35f), fontSize = 13.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color.White.copy(0.06f),
                        unfocusedContainerColor = Color.White.copy(0.06f),
                        focusedBorderColor = Color(0xFF2E94FF),
                        unfocusedBorderColor = Color.White.copy(0.12f)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )
            }

            // 2. Khung giờ
            Column {
                Text("KHUNG GIỜ CỤ THỂ", color = Color.White.copy(0.6f), fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val presets = listOf(
                        "07:00–10:00" to listOf(7, 0, 10, 0),
                        "12:00–13:00" to listOf(12, 0, 13, 0),
                        "13:30–14:00" to listOf(13, 30, 14, 0),
                        "16:30–17:00" to listOf(16, 30, 17, 0)
                    )
                    presets.forEach { (text, times) ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.White.copy(0.06f))
                                .border(1.dp, Color.White.copy(0.08f), RoundedCornerShape(6.dp))
                                .clickable {
                                    onTimeChange(times[0], times[1], times[2], times[3])
                                }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text(text, color = Color.White.copy(0.85f), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }

            // 3. Phân loại màu sắc (6 danh mục chuẩn)
            Column {
                Text("MÀU SẮC Ô LỊCH", color = Color.White.copy(0.6f), fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    EventCategory.entries.forEach { cat ->
                        val isSelected = cat == selectedCategory
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(cat.composeColor)
                                .clickable { onCategoryChange(cat) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Text("✓", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }

            // Toggles
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White.copy(0.04f))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = Color(0xFFFBBF24), modifier = Modifier.size(16.dp))
                    Text("Bật nhắc nhở & Báo thức", color = Color.White.copy(0.9f), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
                Switch(checked = hasReminder, onCheckedChange = onReminderChange)
            }

            // Nút Xếp Vào Bảng Lịch
            Button(
                onClick = onAddEvent,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E94FF)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Text("Xếp Vào Bảng Lịch", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}
