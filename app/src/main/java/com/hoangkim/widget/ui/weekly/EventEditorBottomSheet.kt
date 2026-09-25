package com.hoangkim.widget.ui.weekly

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hoangkim.widget.model.CalendarEvent
import com.hoangkim.widget.model.EventCategory
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventEditorBottomSheet(
    event: CalendarEvent? = null,
    initialDate: LocalDate = LocalDate.now(),
    onDismiss: () -> Unit,
    onSave: (CalendarEvent) -> Unit,
    onDelete: ((CalendarEvent) -> Unit)? = null
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val isEditing = event != null

    var title by remember { mutableStateOf(event?.title ?: "") }
    var location by remember { mutableStateOf(event?.location ?: "") }
    var category by remember { mutableStateOf(event?.category ?: EventCategory.OTHER) }
    var eventDate by remember { mutableStateOf(event?.startDate?.toLocalDate() ?: initialDate) }
    var startHour by remember { mutableIntStateOf(event?.startDate?.hour ?: 8) }
    var startMinute by remember { mutableIntStateOf(event?.startDate?.minute ?: 0) }
    var endHour by remember { mutableIntStateOf(event?.endDate?.hour ?: 10) }
    var endMinute by remember { mutableIntStateOf(event?.endDate?.minute ?: 0) }
    var isAllDay by remember { mutableStateOf(event?.isAllDay ?: false) }
    var isRecurringWeekly by remember { mutableStateOf(event?.isRecurringWeekly ?: true) }
    var hasReminder by remember { mutableStateOf(event?.hasReminder ?: true) }

    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    // Tự động phân loại danh mục nếu người dùng đang tạo mới và chưa chọn danh mục khác
    var hasManuallyPickedCategory by remember { mutableStateOf(isEditing) }

    fun updateTitle(newTitle: String) {
        title = newTitle
        if (!hasManuallyPickedCategory && newTitle.isNotBlank()) {
            val inferred = EventCategory.infer(newTitle)
            if (inferred != EventCategory.OTHER) {
                category = inferred
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF14171F),
        dragHandle = {
            BottomSheetDefaults.DragHandle(color = Color.White.copy(alpha = 0.3f))
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .padding(horizontal = 18.dp)
        ) {
            // Header Top Bar: Hủy - Tiêu Đề - Lưu
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Hủy", color = Color.White.copy(alpha = 0.7f), fontSize = 15.sp)
                }

                Text(
                    text = if (isEditing) "Sửa Lịch Trình" else "Thêm Lịch Mới",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )

                Button(
                    onClick = {
                        val trimmedTitle = title.trim()
                        if (trimmedTitle.isEmpty()) {
                            Toast.makeText(context, "Vui lòng nhập tên công việc hoặc môn học!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (!isAllDay && (endHour < startHour || (endHour == startHour && endMinute <= startMinute))) {
                            Toast.makeText(context, "Giờ kết thúc phải sau giờ bắt đầu!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        val sDate = if (isAllDay) {
                            LocalDateTime.of(eventDate, LocalTime.of(0, 0))
                        } else {
                            LocalDateTime.of(eventDate, LocalTime.of(startHour, startMinute))
                        }
                        val eDate = if (isAllDay) {
                            LocalDateTime.of(eventDate, LocalTime.of(23, 59))
                        } else {
                            LocalDateTime.of(eventDate, LocalTime.of(endHour, endMinute))
                        }

                        val resultEvent = CalendarEvent(
                            id = event?.id ?: UUID.randomUUID().toString(),
                            title = trimmedTitle,
                            startDate = sDate,
                            endDate = eDate,
                            category = category,
                            isAllDay = isAllDay,
                            isRecurringWeekly = isRecurringWeekly,
                            recurrenceEndDate = event?.recurrenceEndDate,
                            hasReminder = hasReminder,
                            location = location.trim()
                        )
                        onSave(resultEvent)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E94FF)),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text("Lưu", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }

            // Scrollable Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Hero Banner phong cách Apple
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    category.composeColor.copy(alpha = 0.22f),
                                    Color.White.copy(alpha = 0.04f)
                                )
                            )
                        )
                        .border(1.dp, category.composeColor.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(category.composeColor, category.composeColor.copy(alpha = 0.7f))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when (category) {
                                EventCategory.WORK -> "💼"
                                EventCategory.PERSONAL -> "👤"
                                EventCategory.HEALTH -> "🏃"
                                EventCategory.STUDY -> "📚"
                                EventCategory.FAMILY -> "👨‍👩‍👧"
                                EventCategory.OTHER -> "⚡"
                            },
                            fontSize = 24.sp
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (title.isBlank()) "Chi tiết lịch trình" else title,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Danh mục: ${category.displayName} • ${if (isRecurringWeekly) "Lặp lại mỗi tuần" else "Một lần"}",
                            color = category.composeColor,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // 1. CARD THÔNG TIN CHÍNH
                EditorSectionCard(title = "THÔNG TIN CHUNG") {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { updateTitle(it) },
                        placeholder = { Text("Tên môn học / cuộc họp / công việc...", color = Color.White.copy(alpha = 0.4f), fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.EditNote, contentDescription = null, tint = category.composeColor) },
                        trailingIcon = {
                            if (title.isNotEmpty()) {
                                IconButton(onClick = { updateTitle("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Xóa", tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(18.dp))
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = category.composeColor,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                            focusedContainerColor = Color.White.copy(alpha = 0.05f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.03f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        placeholder = { Text("Địa điểm / Phòng học / Link online (tùy chọn)", color = Color.White.copy(alpha = 0.4f), fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Place, contentDescription = null, tint = Color.White.copy(alpha = 0.6f)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF2E94FF),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                            focusedContainerColor = Color.White.copy(alpha = 0.05f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.03f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    // Dải chọn 6 danh mục
                    Text(
                        text = "Danh mục phân loại:",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        EventCategory.entries.forEach { cat ->
                            val isSelected = cat == category
                            val emoji = when (cat) {
                                EventCategory.WORK -> "💼"
                                EventCategory.PERSONAL -> "👤"
                                EventCategory.HEALTH -> "🏃"
                                EventCategory.STUDY -> "📚"
                                EventCategory.FAMILY -> "👨‍👩‍👧"
                                EventCategory.OTHER -> "⚡"
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) cat.composeColor.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.06f))
                                    .border(
                                        width = if (isSelected) 1.8.dp else 1.dp,
                                        color = if (isSelected) cat.composeColor else Color.White.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable {
                                        hasManuallyPickedCategory = true
                                        category = cat
                                    }
                                    .padding(horizontal = 10.dp, vertical = 7.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Text(emoji, fontSize = 12.sp)
                                    Text(
                                        text = cat.displayName,
                                        color = if (isSelected) cat.composeColor else Color.White.copy(alpha = 0.8f),
                                        fontSize = 11.5.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }

                // 2. CARD THỜI GIAN & NGÀY DIỄN RA
                EditorSectionCard(title = "THỜI GIAN & NGÀY") {
                    // Cả ngày Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.AccessTime, contentDescription = null, tint = Color(0xFF47C5E2), modifier = Modifier.size(18.dp))
                            Text("Sự kiện cả ngày", color = Color.White, fontSize = 13.sp)
                        }
                        Switch(
                            checked = isAllDay,
                            onCheckedChange = { isAllDay = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF2E94FF))
                        )
                    }

                    // Chọn ngày diễn ra
                    val dayFormatter = DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White.copy(alpha = 0.05f))
                            .clickable {
                                DatePickerDialog(
                                    context,
                                    { _, year, month, dayOfMonth ->
                                        eventDate = LocalDate.of(year, month + 1, dayOfMonth)
                                    },
                                    eventDate.year,
                                    eventDate.monthValue - 1,
                                    eventDate.dayOfMonth
                                ).show()
                            }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color(0xFF55B5FF), modifier = Modifier.size(16.dp))
                            Text(
                                text = eventDate.format(dayFormatter),
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Text("Đổi ngày", color = Color(0xFF55B5FF), fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    }

                    // Nếu không phải cả ngày: Hiển thị 2 box giờ bắt đầu / kết thúc
                    if (!isAllDay) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Giờ bắt đầu
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White.copy(alpha = 0.05f))
                                    .border(1.dp, Color(0xFF2E94FF).copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                    .clickable {
                                        TimePickerDialog(context, { _, h, m ->
                                            startHour = h
                                            startMinute = m
                                            if (endHour < h || (endHour == h && endMinute <= m)) {
                                                endHour = (h + 1) % 24
                                                endMinute = m
                                            }
                                        }, startHour, startMinute, true).show()
                                    }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("BẮT ĐẦU", fontSize = 9.sp, color = Color.White.copy(alpha = 0.5f), fontWeight = FontWeight.Bold)
                                    Text(
                                        text = String.format("%02d:%02d", startHour, startMinute),
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF55B5FF),
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }

                            // Giờ kết thúc
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White.copy(alpha = 0.05f))
                                    .border(1.dp, Color(0xFF2E94FF).copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                    .clickable {
                                        TimePickerDialog(context, { _, h, m ->
                                            endHour = h
                                            endMinute = m
                                        }, endHour, endMinute, true).show()
                                    }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("KẾT THÚC", fontSize = 9.sp, color = Color.White.copy(alpha = 0.5f), fontWeight = FontWeight.Bold)
                                    Text(
                                        text = String.format("%02d:%02d", endHour, endMinute),
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF55B5FF),
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }

                        // Các nút chọn thời lượng nhanh (Quick Durations)
                        Text(
                            text = "Thời lượng nhanh:",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 10.5.sp
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(
                                30 to "+30p",
                                45 to "+45p",
                                60 to "+1h",
                                90 to "+1.5h",
                                120 to "+2h"
                            ).forEach { (minutes, label) ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.White.copy(alpha = 0.06f))
                                        .clickable {
                                            val totalStartMins = startHour * 60 + startMinute
                                            val totalEndMins = (totalStartMins + minutes) % (24 * 60)
                                            endHour = totalEndMins / 60
                                            endMinute = totalEndMins % 60
                                        }
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(label, color = Color.White.copy(alpha = 0.85f), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }

                // 3. CARD LẶP LẠI & BÁO THỨC
                EditorSectionCard(title = "LẶP LẠI & BÁO THỨC") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Lặp lại hàng tuần", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text("Tự động xuất hiện vào cùng thứ này hàng tuần", color = Color.White.copy(alpha = 0.5f), fontSize = 10.5.sp)
                        }
                        Switch(
                            checked = isRecurringWeekly,
                            onCheckedChange = { isRecurringWeekly = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF2E94FF))
                        )
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.08f), thickness = 0.8.dp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Bật nhắc nhở & báo thức", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text("Đổ chuông hệ thống nhắc nhở trước giờ sự kiện", color = Color.White.copy(alpha = 0.5f), fontSize = 10.5.sp)
                        }
                        Switch(
                            checked = hasReminder,
                            onCheckedChange = { hasReminder = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFFFACB24))
                        )
                    }
                }

                // 4. XEM TRƯỚC THẺ LỊCH
                EditorSectionCard(title = "XEM TRƯỚC HIỂN THỊ") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1E212B))
                            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(38.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(category.composeColor)
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (title.isBlank()) "Tên sự kiện" else title,
                                color = Color.White,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isAllDay) "Cả ngày" else String.format("%02d:%02d - %02d:%02d", startHour, startMinute, endHour, endMinute) +
                                        if (location.isNotBlank()) " • $location" else "",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 11.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(category.composeColor.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(category.displayName, color = category.composeColor, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // 5. NÚT XÓA SỰ KIỆN (NẾU ĐANG CHỈNH SỬA)
                if (isEditing && onDelete != null) {
                    Button(
                        onClick = { showDeleteConfirmDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF453A).copy(alpha = 0.15f))
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFFF453A), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Xóa Lịch Trình Này", color = Color(0xFFFF453A), fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // Dialog Xác Nhận Xóa
    if (showDeleteConfirmDialog && event != null && onDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Xác Nhận Xóa Lịch Trình", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = { Text("Bạn có chắc chắn muốn xóa sự kiện \"${event.title}\"? Hành động này không thể hoàn tác.", fontSize = 13.sp) },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDelete(event)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF453A))
                ) {
                    Text("Xóa Ngay", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }
}

@Composable
private fun EditorSectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF1E212B))
                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            content = content
        )
    }
}
