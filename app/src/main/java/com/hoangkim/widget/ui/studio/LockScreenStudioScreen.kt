package com.hoangkim.widget.ui.studio

import android.app.WallpaperManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
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
    FONT("Chữ", "🔤"),
    COLOR("Màu Sắc", "🌈"),
    POSITION("Vị Trí", "↕️")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LockScreenStudioScreen(
    viewModel: com.hoangkim.widget.viewmodel.StudioViewModel,
    onBack: () -> Unit
) {
    LockScreenStudioScreen(
        repository = viewModel.repository,
        onBack = onBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LockScreenStudioScreen(
    repository: EventRepository,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val events by repository.events.collectAsState()

    // Chặn vuốt Back Android thoát app đột ngột
    BackHandler(onBack = onBack)

    var config by remember { mutableStateOf(repository.loadWallpaperConfig()) }
    var selectedTab by remember { mutableStateOf(StudioTab.BACKGROUND) }
    var customUserBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var customHexInput by remember { mutableStateOf(config.customHex ?: "") }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val bmp = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, it))
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, it)
                }
                customUserBitmap = bmp
                config = config.copy(preset = WallpaperPreset.CUSTOM)
                Toast.makeText(context, "Đã chọn ảnh nền cá nhân!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Lỗi đọc ảnh: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val (deviceWidth, deviceHeight) = remember { FindX9WallpaperRenderer.getDeviceScreenDimensions(context) }


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
            ) {
                if (config.preset == WallpaperPreset.CUSTOM && customUserBitmap != null) {
                    Image(
                        bitmap = customUserBitmap!!.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.45f))
                    )
                }

                Column(
                    modifier = Modifier.fillMaxSize().padding(12.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Cụm Nốt Ruồi Camera Selfie & Đồng hồ ColorOS
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color.Black)
                            .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape)
                    )

                    val currentTimeStr = remember { java.time.LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")) }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(
                            text = currentTimeStr,
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
                    CompositionLocalProvider(LocalTextStyle provides LocalTextStyle.current.copy(fontFamily = config.fontTheme.fontFamily)) {
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
                                        text = if (config.layoutType == CalendarLayoutType.ROWS) "LỊCH TRÌNH TUẦN" else if (config.layoutType == CalendarLayoutType.FROSTED) "💎 KÍNH MỜ • TUẦN NÀY" else "📅 BẢNG 7 CỘT TUẦN",
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
                                // 1. Sub-Tab Nền: Ảnh Cá Nhân & Các Preset Gradient
                                Row(
                                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Nút Chọn Ảnh Cá Nhân
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.clickable { photoPickerLauncher.launch("image/*") }
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(Color.White.copy(alpha = 0.12f))
                                                .border(
                                                    width = if (config.preset == WallpaperPreset.CUSTOM) 2.5.dp else 1.dp,
                                                    color = if (config.preset == WallpaperPreset.CUSTOM) accent else Color.White.copy(alpha = 0.2f),
                                                    shape = CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Ảnh Bạn",
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = if (config.preset == WallpaperPreset.CUSTOM) accent else Color.White.copy(alpha = 0.7f)
                                        )
                                    }

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
                            StudioTab.FONT -> {
                                // 3. Sub-Tab Chữ: 4 Phông Chữ Nghệ Thuật (Bo Tròn, Hiện Đại, Cổ Điển, Coder)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    CalendarFontTheme.entries.forEach { font ->
                                        val isSelected = config.fontTheme == font
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(55.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(if (isSelected) accent else Color.White.copy(alpha = 0.08f))
                                                .clickable { config = config.copy(fontTheme = font) }
                                                .padding(6.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(font.iconEmoji, fontSize = 15.sp)
                                                Text(
                                                    text = font.title,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    fontFamily = font.fontFamily,
                                                    color = if (isSelected) Color.Black else Color.White
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            StudioTab.COLOR -> {
                                // 4. Sub-Tab Màu Sắc: Chuẩn iOS + Color Hunt + Ô Nhập HEX
                                Row(
                                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val clipboardManager = LocalClipboardManager.current

                                    // Ô dán mã HEX
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.White.copy(alpha = 0.08f))
                                            .padding(horizontal = 6.dp, vertical = 4.dp)
                                    ) {
                                        Text("#", color = accent, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        BasicTextField(
                                            value = customHexInput.removePrefix("#"),
                                            onValueChange = {
                                                customHexInput = "#$it"
                                                if (it.length in 6..8) {
                                                    config = config.copy(customHex = "#$it")
                                                }
                                            },
                                            singleLine = true,
                                            textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.SemiBold),
                                            modifier = Modifier.width(55.dp)
                                        )
                                        IconButton(
                                            onClick = {
                                                val clip = clipboardManager.getText()?.text?.trim()
                                                if (!clip.isNullOrEmpty()) {
                                                    val hex = if (clip.startsWith("#")) clip else "#$clip"
                                                    customHexInput = hex
                                                    config = config.copy(customHex = hex)
                                                }
                                            },
                                            modifier = Modifier.size(20.dp)
                                        ) {
                                            Icon(Icons.Default.ContentPaste, contentDescription = "Dán HEX", tint = accent, modifier = Modifier.size(12.dp))
                                        }
                                    }

                                    AccentColorTheme.entries.forEach { theme ->
                                        val isSelected = config.customHex == null && config.accentTheme == theme
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.clickable { config = config.copy(accentTheme = theme, customHex = null) }
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(30.dp)
                                                    .clip(CircleShape)
                                                    .background(theme.color)
                                                    .border(
                                                        width = if (isSelected) 2.5.dp else 0.dp,
                                                        color = Color.White,
                                                        shape = CircleShape
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (isSelected) {
                                                    Text("✓", color = if (theme == AccentColorTheme.WHITE) Color.Black else Color.White, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = theme.title,
                                                fontSize = 8.5.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = if (isSelected) theme.color else Color.White.copy(alpha = 0.7f)
                                            )
                                        }
                                    }

                                    // Color Hunt Presets (Đã kết nối trực tiếp customHex)
                                    ColorHuntPresets.list.forEach { ch ->
                                        val isSelected = config.customHex?.equals(ch.hex, ignoreCase = true) == true
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.clickable {
                                                customHexInput = ch.hex
                                                config = config.copy(customHex = ch.hex)
                                            }
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(30.dp)
                                                    .clip(CircleShape)
                                                    .background(ch.color)
                                                    .border(
                                                        width = if (isSelected) 2.5.dp else 0.dp,
                                                        color = Color.White,
                                                        shape = CircleShape
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (isSelected) {
                                                    Text("✓", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(ch.title, fontSize = 8.5.sp, color = if (isSelected) ch.color else Color.White.copy(alpha = 0.6f))
                                        }
                                    }
                                }
                            }
                            StudioTab.POSITION -> {
                                // 5. Sub-Tab Vị Trí: Mô Tả Chữ Chuẩn iOS
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
                        }
                    }

                    // CÁC NÚT HÀNH ĐỘNG DƯỚI CÙNG
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // 1. Nút Lưu Cấu Hình Studio
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White.copy(alpha = 0.08f))
                                    .clickable {
                                        repository.saveWallpaperConfig(config)
                                        Toast.makeText(context, "💾 Đã lưu cấu hình Studio thành công!", Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Default.Save, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Text("Lưu Cấu Hình", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            // 2. Nút Tải Ảnh Về Thư Viện (MediaStore)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White.copy(alpha = 0.08f))
                                    .clickable {
                                        try {
                                            val bmp = FindX9WallpaperRenderer.renderWallpaper(
                                                baseImage = if (config.preset == WallpaperPreset.CUSTOM) customUserBitmap else null,
                                                events = events,
                                                config = config,
                                                includeSystemMockUi = false,
                                                targetWidth = deviceWidth,
                                                targetHeight = deviceHeight
                                            )
                                            val uri = FindX9WallpaperRenderer.saveWallpaperToGallery(context, bmp)
                                            if (uri != null) {
                                                Toast.makeText(context, "🎉 Đã lưu hình nền vào thư viện ảnh (Pictures/LichTuan)!", Toast.LENGTH_LONG).show()
                                            } else {
                                                Toast.makeText(context, "Lỗi: Không thể lưu ảnh vào máy", Toast.LENGTH_SHORT).show()
                                            }
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Lỗi xuất ảnh: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Default.Download, contentDescription = null, tint = Color(0xFF47C5E2), modifier = Modifier.size(16.dp))
                                    Text("Lưu Ảnh Vào Máy", color = Color(0xFF47C5E2), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // 3. Nút Đặt Làm Hình Nền Khóa Ngay (1 Chạm - Không có đồng hồ đè)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.linearGradient(listOf(accent, accent.copy(alpha = 0.85f)))
                                )
                                .clickable {
                                    try {
                                        val wm = WallpaperManager.getInstance(context)
                                        // includeSystemMockUi = false để ColorOS tự vẽ đồng hồ và vân tay thật
                                        val bmp = FindX9WallpaperRenderer.renderWallpaper(
                                            baseImage = if (config.preset == WallpaperPreset.CUSTOM) customUserBitmap else null,
                                            events = events,
                                            config = config,
                                            includeSystemMockUi = false,
                                            targetWidth = deviceWidth,
                                            targetHeight = deviceHeight
                                        )
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                            wm.setBitmap(bmp, null, true, WallpaperManager.FLAG_LOCK)
                                            Toast.makeText(context, "🎉 Đã cài làm hình nền màn hình khóa thành công!", Toast.LENGTH_LONG).show()
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

