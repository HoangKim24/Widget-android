package com.hoangkim.widget.wallpaper

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.hoangkim.widget.model.CalendarEvent
import com.hoangkim.widget.model.CalendarFontTheme
import com.hoangkim.widget.model.CalendarLayoutType
import com.hoangkim.widget.model.CalendarPosition
import com.hoangkim.widget.model.WallpaperConfig
import com.hoangkim.widget.model.WallpaperPreset
import java.io.OutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Renderer xuất hình nền khóa chuẩn tỷ lệ cho OPPO Find X9 (mặc định 1264 x 2780 px)
 * và tự động co giãn thích ứng với màn hình mọi thiết bị Android (1080p, 2K, 1440p...).
 * Hỗ trợ 3 kiểu lịch (7 Dòng Chi Tiết, 7 Cột Tối Giản, Kính Mờ) và các preset màu sắc Color Hunt.
 */
object FindX9WallpaperRenderer {
    const val SCREEN_WIDTH = 1264
    const val SCREEN_HEIGHT = 2780

    /**
     * Tự động lấy kích thước thực tế của màn hình thiết bị
     */
    fun getDeviceScreenDimensions(context: Context): Pair<Int, Int> {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as? android.view.WindowManager
                val bounds = windowManager?.currentWindowMetrics?.bounds
                if (bounds != null && bounds.width() > 0 && bounds.height() > 0) {
                    Pair(bounds.width(), bounds.height())
                } else {
                    val dm = context.resources.displayMetrics
                    Pair(dm.widthPixels, dm.heightPixels)
                }
            } else {
                val dm = context.resources.displayMetrics
                Pair(dm.widthPixels, dm.heightPixels)
            }
        } catch (e: Exception) {
            Pair(SCREEN_WIDTH, SCREEN_HEIGHT)
        }
    }

    fun renderWallpaper(
        baseImage: Bitmap?,
        events: List<CalendarEvent>,
        config: WallpaperConfig = WallpaperConfig(),
        includeSystemMockUi: Boolean = false,
        targetWidth: Int = SCREEN_WIDTH,
        targetHeight: Int = SCREEN_HEIGHT
    ): Bitmap {
        val output = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        val scaleX = targetWidth / SCREEN_WIDTH.toFloat()
        val scaleY = targetHeight / SCREEN_HEIGHT.toFloat()
        val scaleFont = minOf(scaleX, scaleY)

        // 1. VẼ HÌNH NỀN
        if (baseImage != null) {
            val srcRect = android.graphics.Rect(0, 0, baseImage.width, baseImage.height)
            val dstRect = android.graphics.Rect(0, 0, targetWidth, targetHeight)
            canvas.drawBitmap(baseImage, srcRect, dstRect, null)
            canvas.drawColor(Color.argb(120, 0, 0, 0))
        } else {
            val colors = config.preset.colors.map {
                Color.argb(
                    (it.alpha * 255).toInt(),
                    (it.red * 255).toInt(),
                    (it.green * 255).toInt(),
                    (it.blue * 255).toInt()
                )
            }.toIntArray()

            val gradient = LinearGradient(
                0f, 0f, targetWidth.toFloat(), targetHeight.toFloat(),
                colors, null, Shader.TileMode.CLAMP
            )
            val bgPaint = Paint().apply { shader = gradient }
            canvas.drawRect(0f, 0f, targetWidth.toFloat(), targetHeight.toFloat(), bgPaint)
        }

        // 2. VẼ ĐỒNG HỒ & GIAO DIỆN MÀN HÌNH KHÓA (Mock UI)
        val today = LocalDate.now()
        if (includeSystemMockUi) {
            // Nốt ruồi camera selfie (OPPO Find X9)
            val camPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.BLACK
            }
            val camBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.argb(60, 255, 255, 255)
                style = Paint.Style.STROKE
                strokeWidth = 2f * scaleFont
            }
            canvas.drawCircle(targetWidth / 2f, 54f * scaleY, 13f * scaleFont, camPaint)
            canvas.drawCircle(targetWidth / 2f, 54f * scaleY, 13f * scaleFont, camBorderPaint)

            val timePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = 150f * scaleFont
                typeface = Typeface.create("sans-serif-thin", Typeface.NORMAL)
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("14:30", targetWidth / 2f, 380f * scaleY, timePaint)

            val dateNames = listOf("Chủ Nhật", "Thứ Hai", "Thứ Ba", "Thứ Tư", "Thứ Năm", "Thứ Sáu", "Thứ Bảy")
            val dayOfWeekIndex = today.dayOfWeek.value % 7
            val dateString = "${dateNames[dayOfWeekIndex]}, ${today.dayOfMonth} Thg ${today.monthValue}"

            val datePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.argb(220, 255, 255, 255)
                textSize = 38f * scaleFont
                typeface = getAppTypeface(config, isBold = true)
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(dateString, targetWidth / 2f, 450f * scaleY, datePaint)
        }

        // 3. TÍNH TOÁN VỊ TRÍ CARD LỊCH
        val baseTop = when (config.position) {
            CalendarPosition.TOP -> targetHeight * 0.22f
            CalendarPosition.CENTER -> targetHeight * 0.32f
            CalendarPosition.BOTTOM -> targetHeight * 0.44f
        }
        val cardTop = baseTop + config.fineTuneYOffsetDp * 2.5f * scaleY
        val marginHorizontal = 44f * scaleX
        val cardRight = targetWidth - marginHorizontal
        val cardHeight = 1100f * scaleY
        val cardBottom = cardTop + cardHeight
        val cardRect = RectF(marginHorizontal, cardTop, cardRight, cardBottom)

        // Màu Accent
        val accentColor = Color.argb(
            (config.effectiveColor.alpha * 255).toInt(),
            (config.effectiveColor.red * 255).toInt(),
            (config.effectiveColor.green * 255).toInt(),
            (config.effectiveColor.blue * 255).toInt()
        )

        // Vẽ nền Card
        val cardBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (config.layoutType == CalendarLayoutType.FROSTED) {
                Color.argb(195, 20, 24, 32)
            } else {
                Color.argb(210, 16, 20, 28)
            }
        }
        val cardCorner = 38f * scaleFont
        canvas.drawRoundRect(cardRect, cardCorner, cardCorner, cardBgPaint)

        // Vẽ viền Card
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (config.layoutType == CalendarLayoutType.FROSTED) {
                Color.argb(90, 255, 255, 255)
            } else {
                Color.argb(50, 255, 255, 255)
            }
            style = Paint.Style.STROKE
            strokeWidth = 2.5f * scaleFont
        }
        canvas.drawRoundRect(cardRect, cardCorner, cardCorner, strokePaint)

        // Tuần hiện tại (7 ngày từ Thứ Hai)
        val daysFromMonday = (today.dayOfWeek.value - 1).toLong()
        val monday = today.minusDays(daysFromMonday)
        val weekDays = (0..6).map { monday.plusDays(it.toLong()) }
        val shortDays = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")

        // 4. VẼ THEO BỐ CỤC
        if (config.layoutType == CalendarLayoutType.ROWS) {
            // BỐ CỤC 1: 7 DÒNG CHI TIẾT
            val rowHeight = 90f * scaleY
            var currentY = cardTop + 40f * scaleY

            val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = accentColor
                textSize = 34f * scaleFont
                typeface = getAppTypeface(config, isBold = true)
            }
            canvas.drawText("LỊCH TRÌNH TUẦN", marginHorizontal + 36f * scaleX, currentY, headerPaint)
            currentY += 40f * scaleY

            weekDays.forEachIndexed { idx, day ->
                val isToday = day == today
                val dayEvents = events.filter { it.occurs(day) }.sortedBy { it.startDate }

                // Viên capsule ngày
                val capsuleRect = RectF(
                    marginHorizontal + 24f * scaleX,
                    currentY,
                    marginHorizontal + 170f * scaleX,
                    currentY + 54f * scaleY
                )
                val capsulePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = if (isToday) accentColor else Color.argb(40, 255, 255, 255)
                }
                val capsuleCorner = 27f * scaleFont
                canvas.drawRoundRect(capsuleRect, capsuleCorner, capsuleCorner, capsulePaint)

                val dayTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = if (isToday) Color.BLACK else accentColor
                    textSize = 24f * scaleFont
                    typeface = getAppTypeface(config, isBold = true)
                    textAlign = Paint.Align.CENTER
                }
                canvas.drawText("${shortDays[idx]} ${day.dayOfMonth}", capsuleRect.centerX(), capsuleRect.centerY() + 8f * scaleY, dayTextPaint)

                // Sự kiện ngang
                if (dayEvents.isNotEmpty()) {
                    var chipX = marginHorizontal + 185f * scaleX
                    val chipWidth = 340f * scaleX
                    dayEvents.take(2).forEach { ev ->
                        val chipRect = RectF(chipX, currentY, chipX + chipWidth, currentY + 54f * scaleY)
                        val chipBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                            val cat = ev.category.composeColor
                            color = Color.argb(220, (cat.red * 255).toInt(), (cat.green * 255).toInt(), (cat.blue * 255).toInt())
                        }
                        canvas.drawRoundRect(chipRect, 14f * scaleFont, 14f * scaleFont, chipBgPaint)

                        val timeFmt = ev.startDate.format(DateTimeFormatter.ofPattern("HH:mm"))
                        val evTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                            color = Color.WHITE
                            textSize = 21f * scaleFont
                            typeface = getAppTypeface(config, isBold = true)
                        }
                        val titleSnippet = if (ev.title.length > 12) ev.title.take(11) + "…" else ev.title
                        canvas.drawText("$timeFmt $titleSnippet", chipX + 16f * scaleX, currentY + 34f * scaleY, evTextPaint)
                        chipX += (chipWidth + 15f * scaleX)
                    }
                } else {
                    val emptyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = Color.argb(70, 255, 255, 255)
                        textSize = 22f * scaleFont
                        typeface = getAppTypeface(config, isBold = false)
                    }
                    canvas.drawText("—", marginHorizontal + 210f * scaleX, currentY + 36f * scaleY, emptyPaint)
                }

                currentY += rowHeight
            }

            // Today section
            val todayEvents = events.filter { it.occurs(today) }
            if (todayEvents.isNotEmpty()) {
                currentY += 15f * scaleY
                val todayTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = accentColor
                    textSize = 28f * scaleFont
                    typeface = getAppTypeface(config, isBold = true)
                }
                canvas.drawText("LỊCH TRÌNH HÔM NAY", marginHorizontal + 36f * scaleX, currentY, todayTitlePaint)
                currentY += 40f * scaleY

                todayEvents.take(2).forEach { ev ->
                    val evPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = Color.WHITE
                        textSize = 24f * scaleFont
                        typeface = getAppTypeface(config, isBold = false)
                    }
                    canvas.drawText("• ${ev.timeRangeFormatted}: ${ev.title}", marginHorizontal + 36f * scaleX, currentY, evPaint)
                    currentY += 34f * scaleY
                }
            }

        } else {
            // BỐ CỤC 2 & 3: 7 CỘT TỐI GIẢN HOẶC KÍNH MỜ
            val colGap = 8f * scaleX
            val colWidth = (cardRight - marginHorizontal - 40f * scaleX) / 7f
            var startX = marginHorizontal + 20f * scaleX
            val headerY = cardTop + 50f * scaleY

            val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = accentColor
                textSize = 34f * scaleFont
                typeface = getAppTypeface(config, isBold = true)
            }
            val titleStr = if (config.layoutType == CalendarLayoutType.FROSTED) "LỊCH THẺ KÍNH MỜ" else "TỔNG QUAN TUẦN NÀY"
            canvas.drawText(titleStr, marginHorizontal + 36f * scaleX, headerY, headerPaint)

            val gridTop = headerY + 40f * scaleY
            weekDays.forEachIndexed { idx, day ->
                val isToday = day == today
                val colRect = RectF(startX, gridTop, startX + colWidth - colGap, gridTop + 540f * scaleY)

                val colBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = if (isToday) Color.argb(60, 46, 148, 255) else Color.argb(25, 255, 255, 255)
                }
                canvas.drawRoundRect(colRect, 18f * scaleFont, 18f * scaleFont, colBgPaint)

                // Header Cột
                val dayNamePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = if (isToday) accentColor else Color.argb(200, 255, 255, 255)
                    textSize = 22f * scaleFont
                    typeface = getAppTypeface(config, isBold = true)
                    textAlign = Paint.Align.CENTER
                }
                canvas.drawText(shortDays[idx], colRect.centerX(), gridTop + 36f * scaleY, dayNamePaint)

                val dayNumPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = if (isToday) accentColor else Color.WHITE
                    textSize = 28f * scaleFont
                    typeface = getAppTypeface(config, isBold = true)
                    textAlign = Paint.Align.CENTER
                }
                canvas.drawText(day.dayOfMonth.toString(), colRect.centerX(), gridTop + 72f * scaleY, dayNumPaint)

                // Events
                val dayEvents = events.filter { it.occurs(day) }.sortedBy { it.startDate }
                var evY = gridTop + 95f * scaleY
                dayEvents.take(4).forEach { ev ->
                    val evRect = RectF(startX + 4f * scaleX, evY, startX + colWidth - 12f * scaleX, evY + 85f * scaleY)
                    val evBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        val cat = ev.category.composeColor
                        color = Color.argb(220, (cat.red * 255).toInt(), (cat.green * 255).toInt(), (cat.blue * 255).toInt())
                    }
                    canvas.drawRoundRect(evRect, 10f * scaleFont, 10f * scaleFont, evBgPaint)

                    val evTimePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = Color.argb(220, 255, 255, 255)
                        textSize = 15f * scaleFont
                        typeface = getAppTypeface(config, isBold = false)
                        textAlign = Paint.Align.CENTER
                    }
                    canvas.drawText(ev.startDate.format(DateTimeFormatter.ofPattern("HH:mm")), evRect.centerX(), evY + 26f * scaleY, evTimePaint)

                    val evTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = Color.WHITE
                        textSize = 17f * scaleFont
                        typeface = getAppTypeface(config, isBold = true)
                        textAlign = Paint.Align.CENTER
                    }
                    val shortTitle = if (ev.title.length > 6) ev.title.take(5) + "…" else ev.title
                    canvas.drawText(shortTitle, evRect.centerX(), evY + 56f * scaleY, evTitlePaint)

                    evY += 95f * scaleY
                }

                startX += colWidth
            }

            // Today summary bên dưới lưới cột
            val todayY = gridTop + 580f * scaleY
            val todayTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = accentColor
                textSize = 28f * scaleFont
                typeface = getAppTypeface(config, isBold = true)
            }
            canvas.drawText("LỊCH TRÌNH HÔM NAY", marginHorizontal + 36f * scaleX, todayY, todayTitlePaint)

            val todayEvents = events.filter { it.occurs(today) }
            var subY = todayY + 40f * scaleY
            if (todayEvents.isNotEmpty()) {
                todayEvents.take(3).forEach { ev ->
                    val evPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = Color.WHITE
                        textSize = 24f * scaleFont
                    }
                    canvas.drawText("• ${ev.timeRangeFormatted}  ${ev.title}", marginHorizontal + 36f * scaleX, subY, evPaint)
                    subY += 34f * scaleY
                }
            } else {
                val freePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.argb(150, 255, 255, 255)
                    textSize = 22f * scaleFont
                }
                canvas.drawText("✨ Hôm nay thảnh thơi • Không có lịch trình", marginHorizontal + 36f * scaleX, subY, freePaint)
            }
        }

        // 5. CẢM BIẾN VÂN TAY QUANG HỌC DƯỚI MÀN HÌNH (Chỉ vẽ khi bật includeSystemMockUi)
        if (includeSystemMockUi) {
            val fpCenterY = targetHeight * (2460f / 2780f)
            val fpRadius = 52f * scaleFont
            val fpPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.argb(40, 255, 255, 255)
                style = Paint.Style.STROKE
                strokeWidth = 3f * scaleFont
            }
            canvas.drawCircle(targetWidth / 2f, fpCenterY, fpRadius, fpPaint)
        }

        return output
    }

    /**
     * Lưu ảnh hình nền độ phân giải cao vào Thư viện ảnh (MediaStore) trong thư mục Pictures/LichTuan
     */
    fun saveWallpaperToGallery(context: Context, bitmap: Bitmap): Uri? {
        val filename = "LichTuan_${System.currentTimeMillis()}.png"
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, filename)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/LichTuan")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues) ?: return null

        try {
            resolver.openOutputStream(uri)?.use { stream: OutputStream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)
            }
            return uri
        } catch (e: Exception) {
            resolver.delete(uri, null, null)
            return null
        }
    }

    private fun getAppTypeface(config: WallpaperConfig, isBold: Boolean): Typeface {
        val style = if (isBold) Typeface.BOLD else Typeface.NORMAL
        return when (config.fontTheme) {
            CalendarFontTheme.ROUNDED -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    try {
                        Typeface.create("sans-serif-rounded", style)
                    } catch (e: Exception) {
                        Typeface.create(Typeface.DEFAULT, style)
                    }
                } else {
                    Typeface.create(Typeface.DEFAULT, style)
                }
            }
            CalendarFontTheme.MODERN -> Typeface.create(Typeface.SANS_SERIF, style)
            CalendarFontTheme.SERIF -> Typeface.create(Typeface.SERIF, style)
            CalendarFontTheme.MONOSPACED -> Typeface.create(Typeface.MONOSPACE, style)
        }
    }
}
