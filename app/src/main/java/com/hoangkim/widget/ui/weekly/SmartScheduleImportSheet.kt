package com.hoangkim.widget.ui.weekly

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.hoangkim.widget.data.ScheduleTextParser
import com.hoangkim.widget.model.CalendarEvent
import com.hoangkim.widget.repository.EventRepository
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartScheduleImportSheet(
    targetMonday: LocalDate,
    repository: EventRepository,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var inputText by remember { mutableStateOf("") }
    var parsedItems by remember { mutableStateOf<List<ScheduleTextParser.ParsedItem>>(emptyList()) }
    var isRecurringWeekly by remember { mutableStateOf(false) }
    var hasReminder by remember { mutableStateOf(true) }
    var isScanningOCR by remember { mutableStateOf(false) }

    // Launcher quét ảnh thời khóa biểu (OCR ML Kit)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            isScanningOCR = true
            try {
                val image = InputImage.fromFilePath(context, it)
                val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                recognizer.process(image)
                    .addOnSuccessListener { visionText ->
                        isScanningOCR = false
                        val recognized = visionText.text
                        if (recognized.isNotEmpty()) {
                            inputText = recognized
                            parsedItems = ScheduleTextParser.parse(recognized)
                            Toast.makeText(context, "✅ Đã trích xuất văn bản từ ảnh thành công!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Không nhận diện được chữ trong ảnh!", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener { e ->
                        isScanningOCR = false
                        Toast.makeText(context, "Lỗi quét ảnh: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } catch (e: Exception) {
                isScanningOCR = false
                Toast.makeText(context, "Không thể đọc tệp ảnh: ${e.message}", Toast.LENGTH_SHORT).show()
            }
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
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFF2E94FF))
                    Text(
                        "Nhập Lịch Thông Minh (AI & OCR)",
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
                "Gõ câu nói tự nhiên, dán tin nhắn Zalo hoặc quét ảnh thời khóa biểu để tự động bóc tách ngày giờ.",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Gợi ý Prompt Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val prompts = listOf(
                    "⚡ 2-4-6 sáng 6h-7h Gym" to "Sáng 2-4-6 tập gym 6h đến 7h",
                    "⚡ 3-5-7 tối 19h-21h Tiếng Anh" to "Tối 3-5-7 học tiếng Anh 19h đến 21h",
                    "⚡ T7 cả ngày Dã ngoại" to "Thứ 7 cả ngày đi dã ngoại",
                    "⚡ T2-T6 8h-17h Đi làm" to "Từ thứ 2 đến thứ 6: 08:00 - 17:00 Đi làm"
                )
                prompts.forEach { (label, text) ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White.copy(alpha = 0.08f))
                            .clickable {
                                inputText = text
                                parsedItems = ScheduleTextParser.parse(text)
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(label, fontSize = 11.5.sp, color = Color(0xFF2E94FF), fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Ô nhập văn bản
            OutlinedTextField(
                value = inputText,
                onValueChange = {
                    inputText = it
                    parsedItems = ScheduleTextParser.parse(it)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp),
                placeholder = {
                    Text(
                        "Dán tin nhắn Zalo hoặc gõ tự do (vd: Sáng 2-4-6 học 7h-9h, T3 tối đi bơi 18h)...",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.35f)
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF2E94FF),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color(0xFF2E94FF)
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Nút bấm hành động nhanh: Quét ảnh OCR & Tùy chọn lặp
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { photoPickerLauncher.launch("image/*") },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFB388FF)),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.horizontalGradient(listOf(Color(0xFFB388FF), Color(0xFF2E94FF)))),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isScanningOCR) "Đang Quét..." else "Quét Ảnh TKB (OCR)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = hasReminder,
                        onCheckedChange = { hasReminder = it },
                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFF2E94FF))
                    )
                    Text("Báo thức", fontSize = 11.sp, color = Color.White.copy(0.85f))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Danh sách các sự kiện đã nhận diện
            if (parsedItems.isNotEmpty()) {
                val selectedCount = parsedItems.count { it.isSelected }
                Text(
                    "ĐÃ NHẬN DIỆN (${parsedItems.size} mục • chọn $selectedCount)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E94FF)
                )
                Spacer(modifier = Modifier.height(6.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(parsedItems, key = { it.id }) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.White.copy(alpha = 0.05f))
                                .clickable {
                                    parsedItems = parsedItems.map {
                                        if (it.id == item.id) it.copy(isSelected = !it.isSelected) else it
                                    }
                                }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = item.isSelected,
                                onCheckedChange = { checked ->
                                    parsedItems = parsedItems.map {
                                        if (it.id == item.id) it.copy(isSelected = checked) else it
                                    }
                                },
                                colors = CheckboxDefaults.colors(checkedColor = Color(0xFF2E94FF))
                            )

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF2E94FF).copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(item.dayName, color = Color(0xFF2E94FF), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                Text(
                                    "${item.timeFormatted} • ${item.category.displayName}",
                                    color = Color.White.copy(0.6f),
                                    fontSize = 10.5.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        val eventsToAdd = parsedItems
                            .filter { it.isSelected }
                            .map { it.toCalendarEvent(targetMonday, isRecurringWeekly, hasReminder) }

                        if (eventsToAdd.isNotEmpty()) {
                            repository.importEvents(eventsToAdd)
                            Toast.makeText(context, "🎉 Đã nạp thành công ${eventsToAdd.size} sự kiện vào lịch!", Toast.LENGTH_SHORT).show()
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
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Nạp $selectedCount Sự Kiện Vào Lịch Tuần", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
