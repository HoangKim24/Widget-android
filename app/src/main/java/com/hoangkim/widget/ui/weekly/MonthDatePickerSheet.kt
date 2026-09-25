package com.hoangkim.widget.ui.weekly

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hoangkim.widget.model.CalendarEvent
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthDatePickerSheet(
    currentSelectedDate: LocalDate,
    events: List<CalendarEvent>,
    onSelectDate: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var displayMonth by remember { mutableStateOf(YearMonth.from(currentSelectedDate)) }
    val today = remember { LocalDate.now() }

    val daysOfWeek = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")

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
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header: Điều hướng Tháng / Năm
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = { displayMonth = displayMonth.minusMonths(1) },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.08f))
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronLeft,
                        contentDescription = "Tháng trước",
                        tint = Color.White
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Tháng ${displayMonth.monthValue} / ${displayMonth.year}",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Chạm ngày để chuyển nhanh lịch tuần",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 11.sp
                    )
                }

                IconButton(
                    onClick = { displayMonth = displayMonth.plusMonths(1) },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.08f))
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Tháng sau",
                        tint = Color.White
                    )
                }
            }

            // Tiêu đề các thứ trong tuần
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                daysOfWeek.forEachIndexed { index, name ->
                    val isWeekend = index >= 5
                    Text(
                        text = name,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isWeekend) Color(0xFFFF5252) else Color.White.copy(alpha = 0.6f)
                    )
                }
            }

            // Tính toán danh sách ngày trong tháng
            val firstDayOfMonth = displayMonth.atDay(1)
            val firstDayOfWeek = (firstDayOfMonth.dayOfWeek.value - 1) // 0 for Monday, 6 for Sunday
            val daysInMonth = displayMonth.lengthOfMonth()

            // Tạo danh sách cell: rỗng phía trước + ngày thật
            val totalCells = firstDayOfWeek + daysInMonth
            val gridDays = (1..totalCells).map { cellIndex ->
                if (cellIndex <= firstDayOfWeek) null
                else displayMonth.atDay(cellIndex - firstDayOfWeek)
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(gridDays) { date ->
                    if (date != null) {
                        val isSelected = date == currentSelectedDate
                        val isToday = date == today
                        val hasEvents = events.any { it.occurs(date) }

                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    when {
                                        isSelected -> Color(0xFF2E94FF)
                                        isToday -> Color(0xFF2E94FF).copy(alpha = 0.18f)
                                        else -> Color.Transparent
                                    }
                                )
                                .border(
                                    width = if (isToday && !isSelected) 1.5.dp else 0.dp,
                                    color = Color(0xFF55B5FF),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    onSelectDate(date)
                                    onDismiss()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = date.dayOfMonth.toString(),
                                    fontSize = 13.5.sp,
                                    fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                    color = when {
                                        isSelected -> Color.White
                                        isToday -> Color(0xFF55B5FF)
                                        date.dayOfWeek == DayOfWeek.SUNDAY -> Color(0xFFFF6B6B)
                                        date.dayOfWeek == DayOfWeek.SATURDAY -> Color(0xFFFFA07A)
                                        else -> Color.White.copy(alpha = 0.9f)
                                    }
                                )
                                if (hasEvents) {
                                    Box(
                                        modifier = Modifier
                                            .padding(top = 2.dp)
                                            .size(4.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) Color.White else Color(0xFF47C5E2))
                                    )
                                }
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.aspectRatio(1f))
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Phím tắt điều hướng nhanh: "Hôm nay", "Tuần này", "Đóng"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        onSelectDate(today)
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF55B5FF)),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF2E94FF).copy(alpha = 0.5f)))
                ) {
                    Icon(imageVector = Icons.Default.Today, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Về Hôm Nay", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f))
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Đóng", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
