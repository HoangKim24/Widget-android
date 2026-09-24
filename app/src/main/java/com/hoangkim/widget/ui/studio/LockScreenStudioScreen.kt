package com.hoangkim.widget.ui.studio

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Wallpaper
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
import com.hoangkim.widget.repository.EventRepository
import com.hoangkim.widget.wallpaper.FindX9WallpaperRenderer
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LockScreenStudioScreen(
    repository: EventRepository,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val events by repository.events.collectAsState()

    Scaffold(
        containerColor = Color(0xFF14171F),
        topBar = {
            TopAppBar(
                title = { Text("Studio Màn Hình Khóa (OPPO Find X9)", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Trở về", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF14171F))
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Preview Màn hình khóa Find X9
            Box(
                modifier = Modifier
                    .weight(1f)
                    .width(320.dp)
                    .clip(RoundedCornerShape(36.dp))
                    .background(Color(0xFF0F1218))
                    .border(2.dp, Color.White.copy(0.15f), RoundedCornerShape(36.dp))
                    .padding(14.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Cụm đồng hồ ColorOS
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(top = 20.dp)
                    ) {
                        Text("14:30", fontSize = 48.sp, fontWeight = FontWeight.Thin, color = Color.White)
                        Text("Thứ Tư, 24 Tháng 9", fontSize = 12.sp, color = Color.White.copy(0.8f))
                    }

                    // Card Lịch Tuần Nằm Trong Safe Zone của Find X9
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFF1A1F2B).copy(0.85f))
                            .border(1.dp, Color.White.copy(0.12f), RoundedCornerShape(18.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                text = "📅 BẢNG LỊCH TUẦN NÀY",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF55B5FF),
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            Text(
                                text = "Được căn tự động vào vùng an toàn (Safe Zone) 26% -> 72% chiều cao, không bị che bởi đồng hồ ColorOS hay vân tay.",
                                fontSize = 11.sp,
                                color = Color.White.copy(0.7f),
                                lineHeight = 16.sp
                            )
                        }
                    }

                    // Khu vực cảm biến vân tay quang học dưới màn hình
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(bottom = 14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .border(1.dp, Color.White.copy(0.3f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("👆", fontSize = 16.sp)
                        }
                        Text("Vân tay quang học", fontSize = 9.sp, color = Color.White.copy(0.4f))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Nút bấm đặt làm hình nền khóa ngay lập tức (1 chạm - tính năng vượt trội của Android)
            Button(
                onClick = {
                    try {
                        val wm = WallpaperManager.getInstance(context)
                        val bmp = FindX9WallpaperRenderer.renderWallpaper(null, events, 3)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            wm.setBitmap(bmp, null, true, WallpaperManager.FLAG_LOCK)
                            Toast.makeText(context, "🎉 Đã cài thẳng làm hình nền màn hình khóa thành công!", Toast.LENGTH_LONG).show()
                        } else {
                            wm.setBitmap(bmp)
                            Toast.makeText(context, "Đã cài làm hình nền!", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Lỗi cài hình nền: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Wallpaper, contentDescription = null)
                    Text("Đặt Làm Hình Nền Khóa Ngay (1 Chạm)", fontWeight = FontWeight.Bold, fontSize = 14.5.sp)
                }
            }
        }
    }
}
