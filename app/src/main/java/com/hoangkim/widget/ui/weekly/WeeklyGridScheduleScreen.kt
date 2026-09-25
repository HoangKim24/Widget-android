package com.hoangkim.widget.ui.weekly

import android.app.TimePickerDialog
import android.app.WallpaperManager
import android.content.Intent
import android.os.Build
import android.widget.Toast
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
import com.hoangkim.widget.wallpaper.FindX9WallpaperRenderer
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklyGridScheduleScreen(
    viewModel: com.hoangkim.widget.viewmodel.WeeklyScheduleViewModel,
    onGoToStudio: () -> Unit = {}
) {
    WeeklyGridScheduleScreen(
        repository = viewModel.repository,
        onGoToStudio = onGoToStudio
    )
}

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
    var newEventLocation by remember { mutableStateOf("") }
    var startHour by remember { mutableIntStateOf(7) }
    var startMinute by remember { mutableIntStateOf(0) }
    var endHour by remember { mutableIntStateOf(10) }
    var endMinute by remember { mutableIntStateOf(0) }
    var selectedCategory by remember { mutableStateOf(EventCategory.WORK) }
    var isRecurringWeekly by remember { mutableStateOf(false) }
    var hasReminder by remember { mutableStateOf(true) }

    // Dialog state
    var showClearConfirmDialog by remember { mutableStateOf(false) }
    var showRestoreDialog by remember { mutableStateOf(false) }
    var restoreJsonInput by remember { mutableStateOf("") }
    var editingEvent by remember { mutableStateOf<CalendarEvent?>(null) }


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
                                val copied = repository.copyWeekToNextWeek(targetMonday)
                                if (copied > 0) {
                                    Toast.makeText(context, "🎉 Đã sao chép thành công $copied lịch trình sang tuần sau!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Tuần này chưa có lịch mới để sao chép!", Toast.LENGTH_SHORT).show()
                                }
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
                                try {
                                    val ics = repository.generateIcsString()
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/calendar"
                                        putExtra(Intent.EXTRA_SUBJECT, "LichTuan.ics")
                                        putExtra(Intent.EXTRA_TEXT, ics)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Xuất file lịch (.ics)"))
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Lỗi xuất file: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Sao Lưu Dữ Liệu (.json)", color = Color.White, fontSize = 13.sp) },
                            leadingIcon = { Icon(Icons.Default.Download, contentDescription = null, tint = Color.White.copy(alpha = 0.8f)) },
                            onClick = {
                                showMoreMenu = false
                                try {
                                    val json = repository.getBackupJson()
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_SUBJECT, "LichTuan_Backup.json")
                                        putExtra(Intent.EXTRA_TEXT, json)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Sao lưu dữ liệu lịch (.json)"))
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Lỗi sao lưu: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Khôi Phục Dữ Liệu (.json)", color = Color.White, fontSize = 13.sp) },
                            leadingIcon = { Icon(Icons.Default.Restore, contentDescription = null, tint = Color.White.copy(alpha = 0.8f)) },
                            onClick = {
                                showMoreMenu = false
                                showRestoreDialog = true
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
                                try {
                                    val wm = WallpaperManager.getInstance(context)
                                    val (sw, sh) = FindX9WallpaperRenderer.getDeviceScreenDimensions(context)
                                    val bmp = FindX9WallpaperRenderer.renderWallpaper(
                                        baseImage = null,
                                        events = events,
                                        targetWidth = sw,
                                        targetHeight = sh
                                    )
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                        wm.setBitmap(bmp, null, true, WallpaperManager.FLAG_LOCK)
                                        Toast.makeText(context, "🎉 Đã cập nhật màn hình khóa thành công!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        wm.setBitmap(bmp)
                                        Toast.makeText(context, "Đã cập nhật màn hình khóa!", Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Lỗi cập nhật màn hình: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )

                        Divider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 4.dp))

                        // Section 4: Xóa Lịch
                        DropdownMenuItem(
                            text = { Text("Xóa Hết Lịch Trình", color = Color(0xFFFF453A), fontSize = 13.sp) },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFFF453A)) },
                            onClick = {
                                showMoreMenu = false
                                showClearConfirmDialog = true
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
            // MARK: - PHẦN 1: BẢNG TỔNG QUAN 7 CỘT TUẦN
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

            // MARK: - PHẦN 2: DANH SÁCH LỊCH TRÌNH ĐÃ XẾP TRONG NGÀY
            item {
                ScheduledSlotsSection(
                    selectedDate = selectedDate,
                    events = eventsForSelectedDate,
                    onDeleteEvent = { repository.removeEvent(it.id) },
                    onEditEvent = { editingEvent = it }
                )
            }

            // MARK: - PHẦN 3: KHUNG XẾP LỊCH TRÌNH VÀO NGÀY ĐANG CHỌN (ScheduleInputCard nâng cấp)
            item {
                ScheduleInputCard(
                    selectedDate = selectedDate,
                    title = newEventTitle,
                    onTitleChange = { newEventTitle = it },
                    location = newEventLocation,
                    onLocationChange = { newEventLocation = it },
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

                        if (endHour < startHour || (endHour == startHour && endMinute <= startMinute)) {
                            Toast.makeText(context, "Giờ kết thúc phải diễn ra sau giờ bắt đầu!", Toast.LENGTH_SHORT).show()
                            return@ScheduleInputCard
                        }

                        val sDate = LocalDateTime.of(selectedDate, LocalTime.of(startHour, startMinute))
                        val eDate = LocalDateTime.of(selectedDate, LocalTime.of(endHour, endMinute))

                        repository.addEvent(
                            CalendarEvent(
                                title = newEventTitle.trim(),
                                location = newEventLocation.trim(),
                                startDate = sDate,
                                endDate = eDate,
                                category = selectedCategory,
                                isRecurringWeekly = isRecurringWeekly,
                                hasReminder = hasReminder
                            )
                        )
                        newEventTitle = ""
                        newEventLocation = ""
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

        // Dialog Xác Nhận Xóa
        if (showClearConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showClearConfirmDialog = false },
                title = { Text("Xác Nhận Xóa Toàn Bộ Lịch", fontWeight = FontWeight.Bold) },
                text = { Text("Bạn có chắc chắn muốn xóa toàn bộ lịch trình không? Thao tác này không thể hoàn tác.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            repository.clearAll()
                            showClearConfirmDialog = false
                            Toast.makeText(context, "Đã xóa toàn bộ lịch trình", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Text("Xóa Tất Cả", color = Color(0xFFFF453A), fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearConfirmDialog = false }) {
                        Text("Hủy")
                    }
                }
            )
        }

        // Dialog Khôi Phục JSON
        if (showRestoreDialog) {
            AlertDialog(
                onDismissRequest = { showRestoreDialog = false },
                title = { Text("Khôi Phục Dữ Liệu (.json)", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Dán chuỗi JSON đã sao lưu vào ô dưới đây:", fontSize = 12.sp)
                        OutlinedTextField(
                            value = restoreJsonInput,
                            onValueChange = { restoreJsonInput = it },
                            placeholder = { Text("Dán nội dung JSON vào đây...") },
                            modifier = Modifier.fillMaxWidth().height(120.dp)
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (restoreJsonInput.trim().isNotEmpty()) {
                                val success = repository.restoreFromJson(restoreJsonInput.trim())
                                if (success) {
                                    Toast.makeText(context, "Khôi phục dữ liệu thành công!", Toast.LENGTH_SHORT).show()
                                    showRestoreDialog = false
                                    restoreJsonInput = ""
                                } else {
                                    Toast.makeText(context, "Dữ liệu JSON không hợp lệ!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    ) {
                        Text("Khôi Phục", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showRestoreDialog = false }) {
                        Text("Hủy")
                    }
                }
            )
        }

        // Dialog Chỉnh Sửa Sự Kiện
        editingEvent?.let { ev ->
            EditEventDialog(
                event = ev,
                onDismiss = { editingEvent = null },
                onSave = { updated ->
                    repository.updateEvent(updated)
                    editingEvent = null
                    Toast.makeText(context, "🎉 Đã cập nhật lịch trình thành công!", Toast.LENGTH_SHORT).show()
                },
                onDelete = {
                    repository.removeEvent(ev.id)
                    editingEvent = null
                    Toast.makeText(context, "Đã xóa lịch trình!", Toast.LENGTH_SHORT).show()
                }
            )
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
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = ev.startDate.format(DateTimeFormatter.ofPattern("HH:mm")),
                                            color = Color.White.copy(0.92f),
                                            fontSize = 7.5.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = ev.title,
                                            color = Color.White,
                                            fontSize = 8.5.sp,
                                            fontWeight = FontWeight.Bold,
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
    onDeleteEvent: (CalendarEvent) -> Unit,
    onEditEvent: (CalendarEvent) -> Unit
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
                    SlotItemView(
                        event = ev,
                        isLive = isLive,
                        onEdit = { onEditEvent(ev) },
                        onDelete = { onDeleteEvent(ev) }
                    )
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
    onEdit: () -> Unit,
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
            .clickable { onEdit() }
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

        IconButton(onClick = onEdit, modifier = Modifier.size(24.dp)) {
            Icon(Icons.Default.Edit, contentDescription = "Sửa", tint = Color(0xFF2E94FF), modifier = Modifier.size(16.dp))
        }

        IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
            Icon(Icons.Default.Delete, contentDescription = "Xóa", tint = Color.White.copy(0.35f), modifier = Modifier.size(16.dp))
        }
    }
}

// KHUNG XẾP LỊCH TRÌNH VÀO NGÀY ĐANG CHỌN (Component 3 - Đã thêm Địa Điểm & Chỉnh Giờ Tự Do)
@Composable
fun ScheduleInputCard(
    selectedDate: LocalDate,
    title: String,
    onTitleChange: (String) -> Unit,
    location: String,
    onLocationChange: (String) -> Unit,
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
            verticalArrangement = Arrangement.spacedBy(10.dp)
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
                Text("TÊN CÔNG VIỆC / MÔN HỌC", color = Color.White.copy(0.6f), fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = onTitleChange,
                    placeholder = { Text("Ví dụ: Toán Cao Cấp, Họp Team, Đi Gym...", color = Color.White.copy(0.35f), fontSize = 13.sp) },
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

            // 2. Địa điểm (📍)
            Column {
                Text("ĐỊA ĐIỂM (TÙY CHỌN)", color = Color.White.copy(0.6f), fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = location,
                    onValueChange = onLocationChange,
                    placeholder = { Text("Ví dụ: Phòng A.201, Tầng 3, Google Meet...", color = Color.White.copy(0.35f), fontSize = 13.sp) },
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

            // 3. Khung giờ & Bộ chọn giờ linh hoạt
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("KHUNG GIỜ CỤ THỂ", color = Color.White.copy(0.6f), fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                    Text("Chạm để đổi giờ", color = Color(0xFF47C5E2), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                }
                Spacer(modifier = Modifier.height(6.dp))

                // Hai nút chọn giờ tự do (TimePickerDialog)
                val cardContext = LocalContext.current
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Giờ bắt đầu
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(0.06f))
                            .border(1.dp, Color(0xFF2E94FF).copy(0.5f), RoundedCornerShape(8.dp))
                            .clickable {
                                TimePickerDialog(cardContext, { _, h, m ->
                                    val newEndH = if (endHour < h || (endHour == h && endMinute <= m)) (h + 2) % 24 else endHour
                                    val newEndM = if (endHour < h || (endHour == h && endMinute <= m)) m else endMinute
                                    onTimeChange(h, m, newEndH, newEndM)
                                }, startHour, startMinute, true).show()
                            }
                            .padding(vertical = 8.dp, horizontal = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("BẮT ĐẦU", fontSize = 8.5.sp, color = Color.White.copy(0.6f), fontWeight = FontWeight.Bold)
                            Text(String.format("%02d:%02d", startHour, startMinute), fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF55B5FF), fontFamily = FontFamily.Monospace)
                        }
                    }

                    // Giờ kết thúc
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(0.06f))
                            .border(1.dp, Color(0xFF2E94FF).copy(0.5f), RoundedCornerShape(8.dp))
                            .clickable {
                                TimePickerDialog(cardContext, { _, h, m ->
                                    onTimeChange(startHour, startMinute, h, m)
                                }, endHour, endMinute, true).show()
                            }
                            .padding(vertical = 8.dp, horizontal = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("KẾT THÚC", fontSize = 8.5.sp, color = Color.White.copy(0.6f), fontWeight = FontWeight.Bold)
                            Text(String.format("%02d:%02d", endHour, endMinute), fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF55B5FF), fontFamily = FontFamily.Monospace)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Các preset thông dụng
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val presets = listOf(
                        "07:00–10:00" to listOf(7, 0, 10, 0),
                        "12:00–13:00" to listOf(12, 0, 13, 0),
                        "13:30–15:30" to listOf(13, 30, 15, 30),
                        "17:00–19:00" to listOf(17, 0, 19, 0)
                    )
                    presets.forEach { (text, times) ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.White.copy(0.04f))
                                .border(1.dp, Color.White.copy(0.08f), RoundedCornerShape(6.dp))
                                .clickable {
                                    onTimeChange(times[0], times[1], times[2], times[3])
                                }
                                .padding(vertical = 5.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text, color = Color.White.copy(0.8f), fontSize = 8.5.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }

            // 4. Phân loại màu sắc (6 danh mục chuẩn)
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("MÀU SẮC Ô LỊCH", color = Color.White.copy(0.6f), fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                    Text(selectedCategory.displayName, color = selectedCategory.composeColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    EventCategory.entries.forEach { cat ->
                        val isSelected = cat == selectedCategory
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(cat.composeColor)
                                .clickable { onCategoryChange(cat) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Text("✓", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
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

// HỘP THOẠI CHỈNH SỬA LỊCH TRÌNH
@Composable
fun EditEventDialog(
    event: CalendarEvent,
    onDismiss: () -> Unit,
    onSave: (CalendarEvent) -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    var editTitle by remember { mutableStateOf(event.title) }
    var editLocation by remember { mutableStateOf(event.location) }
    var editStartHour by remember { mutableIntStateOf(event.startDate.hour) }
    var editStartMinute by remember { mutableIntStateOf(event.startDate.minute) }
    var editEndHour by remember { mutableIntStateOf(event.endDate.hour) }
    var editEndMinute by remember { mutableIntStateOf(event.endDate.minute) }
    var editCategory by remember { mutableStateOf(event.category) }
    var editRecurring by remember { mutableStateOf(event.isRecurringWeekly) }
    var editReminder by remember { mutableStateOf(event.hasReminder) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Chỉnh Sửa Lịch Trình", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Xóa", tint = Color(0xFFFF453A))
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Tên việc / môn học
                OutlinedTextField(
                    value = editTitle,
                    onValueChange = { editTitle = it },
                    label = { Text("Tên môn học / công việc") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Địa điểm
                OutlinedTextField(
                    value = editLocation,
                    onValueChange = { editLocation = it },
                    label = { Text("Địa điểm / Phòng học (tùy chọn)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Giờ bắt đầu & Giờ kết thúc
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(0.06f))
                            .border(1.dp, Color(0xFF2E94FF).copy(0.5f), RoundedCornerShape(8.dp))
                            .clickable {
                                TimePickerDialog(context, { _, h, m ->
                                    editStartHour = h
                                    editStartMinute = m
                                    if (editEndHour < h || (editEndHour == h && editEndMinute <= m)) {
                                        editEndHour = (h + 2) % 24
                                        editEndMinute = m
                                    }
                                }, editStartHour, editStartMinute, true).show()
                            }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("BẮT ĐẦU", fontSize = 8.sp, color = Color.White.copy(0.6f))
                            Text(String.format("%02d:%02d", editStartHour, editStartMinute), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF55B5FF), fontFamily = FontFamily.Monospace)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(0.06f))
                            .border(1.dp, Color(0xFF2E94FF).copy(0.5f), RoundedCornerShape(8.dp))
                            .clickable {
                                TimePickerDialog(context, { _, h, m ->
                                    editEndHour = h
                                    editEndMinute = m
                                }, editEndHour, editEndMinute, true).show()
                            }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("KẾT THÚC", fontSize = 8.sp, color = Color.White.copy(0.6f))
                            Text(String.format("%02d:%02d", editEndHour, editEndMinute), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF55B5FF), fontFamily = FontFamily.Monospace)
                        }
                    }
                }

                // Phân loại màu
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Phân loại:", fontSize = 11.sp, color = Color.White.copy(0.7f))
                    Text(editCategory.displayName, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = editCategory.composeColor)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    EventCategory.entries.forEach { cat ->
                        val isSelected = cat == editCategory
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(cat.composeColor)
                                .clickable { editCategory = cat },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Text("✓", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }

                // Toggles
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Lặp lại hàng tuần", fontSize = 12.sp)
                    Switch(checked = editRecurring, onCheckedChange = { editRecurring = it })
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Bật nhắc nhở & báo thức", fontSize = 12.sp)
                    Switch(checked = editReminder, onCheckedChange = { editReminder = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (editTitle.trim().isEmpty()) {
                        Toast.makeText(context, "Tên công việc không được để trống!", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (editEndHour < editStartHour || (editEndHour == editStartHour && editEndMinute <= editStartMinute)) {
                        Toast.makeText(context, "Giờ kết thúc phải diễn ra sau giờ bắt đầu!", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val eventDate = event.startDate.toLocalDate()
                    val newStart = LocalDateTime.of(eventDate, LocalTime.of(editStartHour, editStartMinute))
                    val newEnd = LocalDateTime.of(eventDate, LocalTime.of(editEndHour, editEndMinute))

                    onSave(
                        event.copy(
                            title = editTitle.trim(),
                            location = editLocation.trim(),
                            startDate = newStart,
                            endDate = newEnd,
                            category = editCategory,
                            isRecurringWeekly = editRecurring,
                            hasReminder = editReminder
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E94FF))
            ) {
                Text("Lưu Thay Đổi", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}

