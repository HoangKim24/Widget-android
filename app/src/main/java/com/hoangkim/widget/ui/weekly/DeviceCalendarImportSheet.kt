package com.hoangkim.widget.ui.weekly

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hoangkim.widget.data.DeviceCalendarSyncManager
import com.hoangkim.widget.repository.EventRepository
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceCalendarImportSheet(
    targetMonday: LocalDate,
    repository: EventRepository,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var hasPermission by remember { mutableStateOf(DeviceCalendarSyncManager.hasCalendarPermission(context)) }
    var fetchedItems by remember { mutableStateOf<List<DeviceCalendarSyncManager.SelectableEvent>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    fun loadEvents() {
        isLoading = true
        fetchedItems = DeviceCalendarSyncManager.fetchEventsForWeek(context, targetMonday)
        isLoading = false
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
        if (isGranted) {
            loadEvents()
        } else {
            Toast.makeText(context, "Cần cấp quyền đọc lịch để đồng bộ sự kiện!", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(hasPermission) {
        if (hasPermission) {
            loadEvents()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF141720),
        scrimColor = Color.Black.copy(alpha = 0.65f),
        dragHandle = {
            BottomSheetDefaults.DragHandle(color = Color.White.copy(alpha = 0.3f))
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = Color(0xFF2E94FF))
                    Text(
                        "Đồng Bộ Lịch Thiết Bị",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Đóng", tint = Color.White.copy(0.7f))
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Đọc các sự kiện có sẵn từ Google Calendar, Lịch hệ thống Android để nạp vào Lịch Tuần.",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (!hasPermission) {
                // Màn hình xin quyền
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.EventBusy,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = Color(0xFF2E94FF)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Cần Quyền Truy Cập Lịch",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Để đọc các sự kiện từ Google Calendar trên máy, vui lòng cấp quyền đọc lịch.",
                        fontSize = 12.sp,
                        color = Color.White.copy(0.7f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { permissionLauncher.launch(Manifest.permission.READ_CALENDAR) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E94FF)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Cấp Quyền Ngay", fontWeight = FontWeight.Bold)
                    }
                }
            } else if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF2E94FF))
                }
            } else if (fetchedItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "✨ Không tìm thấy sự kiện nào trong tuần này trên lịch máy.",
                        fontSize = 12.5.sp,
                        color = Color.White.copy(0.5f)
                    )
                }
            } else {
                val selectedCount = fetchedItems.count { it.isSelected }
                Text(
                    "TÌM THẤY ${fetchedItems.size} SỰ KIỆN (Chọn $selectedCount)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E94FF)
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 240.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(fetchedItems, key = { it.id }) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.White.copy(alpha = 0.05f))
                                .clickable {
                                    fetchedItems = fetchedItems.map {
                                        if (it.id == item.id) it.copy(isSelected = !it.isSelected) else it
                                    }
                                }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = item.isSelected,
                                onCheckedChange = { checked ->
                                    fetchedItems = fetchedItems.map {
                                        if (it.id == item.id) it.copy(isSelected = checked) else it
                                    }
                                },
                                colors = CheckboxDefaults.colors(checkedColor = Color(0xFF2E94FF))
                            )

                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.event.title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                val timeStr = "${item.event.startDate.format(DateTimeFormatter.ofPattern("dd/MM HH:mm"))} - ${item.event.endDate.format(DateTimeFormatter.ofPattern("HH:mm"))}"
                                Text(
                                    if (item.event.location.isNotEmpty()) "$timeStr • 📍 ${item.event.location}" else timeStr,
                                    color = Color.White.copy(0.6f),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        val eventsToImport = fetchedItems.filter { it.isSelected }.map { it.event }
                        if (eventsToImport.isNotEmpty()) {
                            repository.importEvents(eventsToImport)
                            Toast.makeText(context, "🎉 Đã nhập ${eventsToImport.size} sự kiện từ lịch máy!", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        } else {
                            Toast.makeText(context, "Vui lòng chọn ít nhất 1 sự kiện!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E94FF))
                ) {
                    Icon(Icons.Default.Download, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Nhập $selectedCount Sự Kiện Đã Chọn", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
