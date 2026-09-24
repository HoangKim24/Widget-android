package com.hoangkim.widget.ui.studio

import android.app.WallpaperManager
import android.os.Build
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Wallpaper
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
import com.hoangkim.widget.model.*
import com.hoangkim.widget.repository.EventRepository
import com.hoangkim.widget.wallpaper.FindX9WallpaperRenderer
import java.time.LocalDate
import java.time.format.DateTimeFormatter

enum class StudioTab(val title: String, val iconEmoji: String) {
    BACKGROUND("Nền", "🎨"),
    LAYOUT("Kiểu Lịch", "📑"),
    POSITION("Vị Trí", "↕️"),
    COLOR("Màu Sắc", "🎨")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LockScreenStudioScreen(
    repository: EventRepository,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val events by repository.events.collectAsState()

    var config by remember { mutableStateOf(WallpaperConfig()) }
    var selectedTab by remember { mutableStateOf(StudioTab.BACKGROUND) }

    val today = LocalDate.now()
    val daysFromMonday = (today.dayOfWeek.value - 1).toLong()
    val monday = today.minusDays(daysFromMonday)
    val weekDays = remember { (0..6).map { monday.plusDays(it.toLong()) } }
    val shortDays = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")

    val accent = config.effectiveColor

    Scaffold(
        containerColor = Color(0xFF0F1218),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Studio Lịch Khóa", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color(0xFF2E94FF).copy(alpha = 0.2f))
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Text("OPPO Find X9", color = Color(0xFF55B5FF), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Trở về", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F1218))
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // MARK: - PHẦN 1: KHUNG PREVIEW ĐIỆN THOẠI OPPO FIND X9
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(config.preset.brush)
                    .border(2.dp, Color.White.copy(0.18f), RoundedCornerShape(32.dp))
                    .padding(12.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Cụm Đồng hồ ColorOS
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text(
                            text = "14:30",
                            fontSize = 44.sp,
                            fontWeight = FontWeight.Thin,
                            color = Color.White
                        )
                        val dayNamesLong = listOf("Chủ Nhật", "Thứ Hai", "Thứ Ba", "Thứ Tư", "Thứ Năm", "Thứ Sáu", "Thứ Bảy")
                        val dayOfWeekIndex = today.dayOfWeek.value % 7
                        Text(
                            text = "${dayNamesLong[dayOfWeekIndex]}, ${today.dayOfMonth} Thg ${today.monthValue}",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(0.85f)
                        )
                    }

                    // Card Lịch Tuần Di Chuyển Theo Vị Trí
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset(y = config.fineTuneYOffsetDp.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                if (config.layoutType == CalendarLayoutType.FROSTED)
                                    Color(0xFF14171F).copy(alpha = 0.85f)
                                else
                                    Color(0xFF14171F).copy(alpha = 0.92f)
                            )
                            .border(
                                1.dp,
                                if (config.layoutType == CalendarLayoutType.FROSTED) accent.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.12f),
                                RoundedCornerShape(18.dp)
                            )
                            .padding(10.dp)
                    ) {
                        Column {
                            // Header tiêu đề kiểu lịch
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (config.layoutType == CalendarLayoutType.ROWS) "WEEKLY SCHEDULE" else if (config.layoutType == CalendarLayoutType.FROSTED) "💎 KÍNH MỜ • TUẦN NÀY" else "📅 BẢNG 7 CỘT TUẦN",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = accent,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "Hôm nay: ${shortDays[(today.dayOfWeek.value - 1)]} ${today.dayOfMonth}",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White.copy(0.6f)
                                )
                            }

                            // 3 Kiểu Bố Cục Thật Khớp iOS
                            when (config.layoutType) {
                                CalendarLayoutType.ROWS -> {
                                    // BỐ CỤC 1: 7 DÒNG CHI TIẾT
                                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                        weekDays.forEachIndexed { idx, day ->
                                            val isToday = day == today
                                            val dayEvs = events.filter { it.occurs(day) }.sortedBy { it.startDate }

                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(if (isToday) accent.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.03f))
                                                    .padding(horizontal = 4.dp, vertical = 2.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                // Capsule Thứ & Ngày
                                                Box(
                                                    modifier = Modifier
                                                        .clip(CircleShape)
                                                        .background(if (isToday) accent else Color.White.copy(alpha = 0.1f))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = "${shortDays[idx]} ${day.dayOfMonth}",
                                                        fontSize = 8.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isToday) Color.Black else accent
                                                    )
                                                }

                                                // Sự kiện ngang
                                                if (dayEvs.isNotEmpty()) {
                                                    Row(
                                                        modifier = Modifier.weight(1f),
                                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                    ) {
                                                        dayEvs.take(2).forEach { ev ->
                                                            Box(
                                                                modifier = Modifier
                                                                    .clip(RoundedCornerShape(4.dp))
                                                                    .background(ev.category.composeColor)
                                                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                                                            ) {
                                                                Text(
                                                                    text = "${ev.startDate.format(DateTimeFormatter.ofPattern("HH:mm"))} ${ev.title}",
                                                                    color = Color.White,
                                                                    fontSize = 7.5.sp,
                                                                    fontWeight = FontWeight.Bold,
                                                                    maxLines = 1,
                                                                    overflow = TextOverflow.Ellipsis
                                                                )
                                                            }
                                                        }
                                                    }
                                                } else {
                                                    Text("—", color = Color.White.copy(alpha = 0.25f), fontSize = 8.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                                CalendarLayoutType.COLUMNS, CalendarLayoutType.FROSTED -> {
                                    // BỐ CỤC 2 & 3: 7 CỘT TỐI GIẢN HOẶC KÍNH MỜ
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        weekDays.forEachIndexed { idx, day ->
                                            val isToday = day == today
                                            val dayEvs = events.filter { it.occurs(day) }.sortedBy { it.startDate }

                                            Column(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(if (isToday) accent.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.04f))
                                                    .border(
                                                        1.dp,
                                                        if (isToday) accent else Color.White.copy(alpha = 0.06f),
                                                        RoundedCornerShape(6.dp)
                                                    )
                                                    .padding(vertical = 4.dp, horizontal = 1.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text(shortDays[idx], fontSize = 7.5.sp, fontWeight = FontWeight.Bold, color = if (isToday) accent else Color.White.copy(alpha = 0.7f))
                                                Text(day.dayOfMonth.toString(), fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = if (isToday) accent else Color.White)
                                                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(if (isToday) accent else Color.White.copy(alpha = 0.08f)))
                                                Spacer(modifier = Modifier.height(2.dp))
                                                if (dayEvs.isNotEmpty()) {
                                                    dayEvs.take(2).forEach { ev ->
                                                        Box(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .padding(bottom = 1.dp)
                                                                .clip(RoundedCornerShape(3.dp))
                                                                .background(ev.category.composeColor)
                                                                .padding(horizontal = 1.dp, vertical = 1.dp)
                                                        ) {
                                                            Text(
                                                                text = ev.title,
                                                                color = Color.White,
                                                                fontSize = 6.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                maxLines = 1,
                                                                overflow = TextOverflow.Ellipsis,
                                                                textAlign = TextAlign.Center,
                                                                modifier = Modifier.fillMaxWidth()
                                                            )
                                                        }
                                                    }
                                                } else {
                                                    Text("•", color = Color.White.copy(alpha = 0.2f), fontSize = 7.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Vân tay quang học Find X9
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(bottom = 6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .border(1.dp, Color.White.copy(0.25f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("👆", fontSize = 13.sp)
                        }
                    }
                }
            }

            // MARK: - PHẦN 2: BẢNG ĐIỀU KHIỂN NỔI (CONTROL PANEL)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E212B)),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(Color.White.copy(0.12f), Color.White.copy(0.12f))))
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Thanh 4 Sub-Tabs Khớp 100% iOS
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        StudioTab.entries.forEach { tab ->
                            val isSelected = selectedTab == tab
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(CircleShape)
                                    .background(if (isSelected) accent else Color.White.copy(alpha = 0.08f))
                                    .clickable { selectedTab = tab }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Text(tab.iconEmoji, fontSize = 11.sp)
                                    Text(
                                        text = tab.title,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.Black else Color.White.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }

                    // Nội Dung Sub-Tab Đang Chọn
                    Box(modifier = Modifier.fillMaxWidth().height(65.dp), contentAlignment = Alignment.Center) {
                        when (selectedTab) {
                            StudioTab.BACKGROUND -> {
                                // 1. Sub-Tab Nền: Các Preset Hình Nền
                                Row(
                                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    WallpaperPreset.entries.filter { it != WallpaperPreset.CUSTOM }.forEach { preset ->
                                        val isSelected = config.preset == preset
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.clickable { config = config.copy(preset = preset) }
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                                    .background(preset.brush)
                                                    .border(
                                                        width = if (isSelected) 2.5.dp else 1.dp,
                                                        color = if (isSelected) accent else Color.White.copy(alpha = 0.2f),
                                                        shape = CircleShape
                                                    )
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = preset.title,
                                                fontSize = 9.5.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = if (isSelected) accent else Color.White.copy(alpha = 0.7f)
                                            )
                                        }
                                    }
                                }
                            }
                            StudioTab.LAYOUT -> {
                                // 2. Sub-Tab Kiểu Lịch: 3 Bố Cục Thật
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    CalendarLayoutType.entries.forEach { layout ->
                                        val isSelected = config.layoutType == layout
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(55.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(if (isSelected) accent else Color.White.copy(alpha = 0.08f))
                                                .clickable { config = config.copy(layoutType = layout) }
                                                .padding(6.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(layout.iconEmoji, fontSize = 16.sp)
                                                Text(
                                                    text = layout.title,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isSelected) Color.Black else Color.White
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            StudioTab.POSITION -> {
                                // 3. Sub-Tab Vị Trí: Mô Tả Chữ Chuẩn iOS
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        CalendarPosition.entries.forEach { pos ->
                                            val isSelected = config.position == pos
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(if (isSelected) accent else Color.White.copy(alpha = 0.08f))
                                                .clickable {
                                                    config = config.copy(position = pos, fineTuneYOffsetDp = 0f)
                                                }
                                                .padding(vertical = 6.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = pos.title,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = if (isSelected) Color.Black else Color.White
                                                )
                                            }
                                        }

                                        // Nút Tự Căn
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFF2E94FF).copy(alpha = 0.25f))
                                                .clickable {
                                                    config = config.copy(position = CalendarPosition.TOP, fineTuneYOffsetDp = 0f)
                                                }
                                                .padding(horizontal = 8.dp, vertical = 6.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                                Text("🪄", fontSize = 10.sp)
                                                Text("Tự Căn", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF47C5E2))
                                            }
                                        }
                                    }

                                    // Thanh trượt tinh chỉnh Y
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text("Dịch chuyển:", fontSize = 10.sp, color = Color.White.copy(alpha = 0.6f))
                                        Slider(
                                            value = config.fineTuneYOffsetDp,
                                            onValueChange = { config = config.copy(fineTuneYOffsetDp = it) },
                                            valueRange = -60f..60f,
                                            modifier = Modifier.weight(1f),
                                            colors = SliderDefaults.colors(
                                                thumbColor = accent,
                                                activeTrackColor = accent
                                            )
                                        )
                                        Text("${config.fineTuneYOffsetDp.toInt()}dp", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.8f))
                                    }
                                }
                            }
                            StudioTab.COLOR -> {
                                // 4. Sub-Tab Màu Sắc: Chuẩn iOS + Color Hunt
                                Row(
                                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AccentColorTheme.entries.forEach { theme ->
                                        val isSelected = config.accentTheme == theme
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.clickable { config = config.copy(accentTheme = theme) }
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .clip(CircleShape)
                                                    .background(theme.color)
                                                    .border(
                                                        width = if (isSelected) 2.5.dp else 0.dp,
                                                        color = Color.White,
                                                        shape = CircleShape
                                                    )
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = theme.title,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = if (isSelected) theme.color else Color.White.copy(alpha = 0.7f)
                                            )
                                        }
                                    }

                                    // Color Hunt Presets
                                    ColorHuntPresets.list.forEach { ch ->
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.clickable {
                                                // Tìm theme tương ứng hoặc đổi màu
                                                val matched = AccentColorTheme.entries.firstOrNull { it.color == ch.color }
                                                if (matched != null) config = config.copy(accentTheme = matched)
                                            }
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .clip(CircleShape)
                                                    .background(ch.color)
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(ch.title, fontSize = 9.sp, color = Color.White.copy(alpha = 0.6f))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 2 NÚT HÀNH ĐỘNG DƯỚI CÙNG
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Nút Cập Nhật
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.linearGradient(listOf(Color(0xFF2E94FF), Color(0xFF6C5CE7)))
                                )
                                .clickable {
                                    Toast.makeText(context, "Đã cập nhật cấu hình Studio!", Toast.LENGTH_SHORT).show()
                                }
                                .padding(vertical = 11.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("⚡ Cập Nhật", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }

                        // Nút Đặt Làm Hình Nền Khóa Ngay (1 Chạm)
                        Box(
                            modifier = Modifier
                                .weight(1.5f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.linearGradient(listOf(accent, accent.copy(alpha = 0.85f)))
                                )
                                .clickable {
                                    try {
                                        val wm = WallpaperManager.getInstance(context)
                                        val bmp = FindX9WallpaperRenderer.renderWallpaper(null, events, config)
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                            wm.setBitmap(bmp, null, true, WallpaperManager.FLAG_LOCK)
                                            Toast.makeText(context, "🎉 Đã cài thẳng làm hình nền màn hình khóa!", Toast.LENGTH_LONG).show()
                                        } else {
                                            wm.setBitmap(bmp)
                                            Toast.makeText(context, "Đã cài làm hình nền!", Toast.LENGTH_SHORT).show()
                                        }
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Lỗi cài hình nền: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                .padding(vertical = 11.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.Wallpaper, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                                Text(
                                    "Đặt Hình Nền Khóa (1 Chạm)",
                                    color = Color.Black,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
