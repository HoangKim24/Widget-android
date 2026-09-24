package com.hoangkim.widget.wallpaper

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import com.hoangkim.widget.model.CalendarEvent
import com.hoangkim.widget.model.CalendarLayoutType
import com.hoangkim.widget.model.CalendarPosition
import com.hoangkim.widget.model.WallpaperConfig
import com.hoangkim.widget.model.WallpaperPreset
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * Renderer xuất hình nền khóa chuẩn tỷ lệ 19.8:9 cho OPPO Find X9 (1264 x 2780 px).
 * Hỗ trợ 3 kiểu lịch (7 Dòng Chi Tiết, 7 Cột Tối Giản, Kính Mờ) và các preset màu sắc Color Hunt.
 */
object FindX9WallpaperRenderer {
    const val SCREEN_WIDTH = 1264
    const val SCREEN_HEIGHT = 2780

    fun renderWallpaper(
        baseImage: Bitmap?,
        events: List<CalendarEvent>,
        config: WallpaperConfig = WallpaperConfig()
    ): Bitmap {
        val output = Bitmap.createBitmap(SCREEN_WIDTH, SCREEN_HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        // 1. VẼ HÌNH NỀN
        if (baseImage != null) {
            val srcRect = android.graphics.Rect(0, 0, baseImage.width, baseImage.height)
            val dstRect = android.graphics.Rect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT)
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
                0f, 0f, SCREEN_WIDTH.toFloat(), SCREEN_HEIGHT.toFloat(),
                colors, null, Shader.TileMode.CLAMP
            )
            val bgPaint = Paint().apply { shader = gradient }
            canvas.drawRect(0f, 0f, SCREEN_WIDTH.toFloat(), SCREEN_HEIGHT.toFloat(), bgPaint)
        }

        // 2. VẼ ĐỒNG HỒ COLOROS (14:30 & Thứ Tư, 24 Tháng 9)
        val today = LocalDate.now()
        val timePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 150f
            typeface = Typeface.create("sans-serif-thin", Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("14:30", SCREEN_WIDTH / 2f, 380f, timePaint)

        val dateNames = listOf("Chủ Nhật", "Thứ Hai", "Thứ Ba", "Thứ Tư", "Thứ Năm", "Thứ Sáu", "Thứ Bảy")
        val dayOfWeekIndex = today.dayOfWeek.value % 7
        val dateString = "${dateNames[dayOfWeekIndex]}, ${today.dayOfMonth} Thg ${today.monthValue}"

        val datePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(220, 255, 255, 255)
            textSize = 38f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(dateString, SCREEN_WIDTH / 2f, 450f, datePaint)

        // 3. TÍNH TOÁN VỊ TRÍ CARD LỊCH
        val baseTop = when (config.position) {
            CalendarPosition.TOP -> SCREEN_HEIGHT * 0.22f
            CalendarPosition.CENTER -> SCREEN_HEIGHT * 0.32f
            CalendarPosition.BOTTOM -> SCREEN_HEIGHT * 0.44f
        }
        val cardTop = baseTop + config.fineTuneYOffsetDp * 2.5f
        val marginHorizontal = 44f
        val cardRight = SCREEN_WIDTH - marginHorizontal
        val cardHeight = 1100f
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
        canvas.drawRoundRect(cardRect, 38f, 38f, cardBgPaint)

        // Vẽ viền Card
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (config.layoutType == CalendarLayoutType.FROSTED) {
                Color.argb(90, 255, 255, 255)
            } else {
                Color.argb(50, 255, 255, 255)
            }
            style = Paint.Style.STROKE
            strokeWidth = 2.5f
        }
        canvas.drawRoundRect(cardRect, 38f, 38f, strokePaint)

        // Tuần hiện tại (7 ngày từ Thứ Hai)
        val daysFromMonday = (today.dayOfWeek.value - 1).toLong()
        val monday = today.minusDays(daysFromMonday)
        val weekDays = (0..6).map { monday.plusDays(it.toLong()) }
        val shortDays = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")

        // 4. VẼ THEO BỐ CỤC
        if (config.layoutType == CalendarLayoutType.ROWS) {
            // BỐ CỤC 1: 7 DÒNG CHI TIẾT
            val rowHeight = 90f
            var currentY = cardTop + 40f

            val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = accentColor
                textSize = 34f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            canvas.drawText("WEEKLY SCHEDULE", marginHorizontal + 36f, currentY, headerPaint)
            currentY += 40f

            weekDays.forEachIndexed { idx, day ->
                val isToday = day == today
                val dayEvents = events.filter { it.occurs(day) }.sortedBy { it.startDate }

                // Viên capsule ngày
                val capsuleRect = RectF(
                    marginHorizontal + 24f,
                    currentY,
                    marginHorizontal + 170f,
                    currentY + 54f
                )
                val capsulePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = if (isToday) accentColor else Color.argb(40, 255, 255, 255)
                }
                canvas.drawRoundRect(capsuleRect, 27f, 27f, capsulePaint)

                val dayTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = if (isToday) Color.BLACK else accentColor
                    textSize = 24f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    textAlign = Paint.Align.CENTER
                }
                canvas.drawText("${shortDays[idx]} ${day.dayOfMonth}", capsuleRect.centerX(), capsuleRect.centerY() + 8f, dayTextPaint)

                // Sự kiện ngang
                if (dayEvents.isNotEmpty()) {
                    var chipX = marginHorizontal + 185f
                    dayEvents.take(2).forEach { ev ->
                        val chipRect = RectF(chipX, currentY, chipX + 340f, currentY + 54f)
                        val chipBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                            val cat = ev.category.composeColor
                            color = Color.argb(220, (cat.red * 255).toInt(), (cat.green * 255).toInt(), (cat.blue * 255).toInt())
                        }
                        canvas.drawRoundRect(chipRect, 14f, 14f, chipBgPaint)

                        val timeFmt = ev.startDate.format(DateTimeFormatter.ofPattern("HH:mm"))
                        val evTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                            color = Color.WHITE
                            textSize = 21f
                            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                        }
                        val titleSnippet = if (ev.title.length > 12) ev.title.take(11) + "…" else ev.title
                        canvas.drawText("$timeFmt $titleSnippet", chipX + 16f, currentY + 34f, evTextPaint)
                        chipX += 355f
                    }
                } else {
                    val emptyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = Color.argb(70, 255, 255, 255)
                        textSize = 22f
                    }
                    canvas.drawText("—", marginHorizontal + 210f, currentY + 36f, emptyPaint)
                }

                currentY += rowHeight
            }

            // Today section
            val todayEvents = events.filter { it.occurs(today) }
            if (todayEvents.isNotEmpty()) {
                currentY += 15f
                val todayTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = accentColor
                    textSize = 28f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                }
                canvas.drawText("TODAY AGENDA", marginHorizontal + 36f, currentY, todayTitlePaint)
                currentY += 40f

                todayEvents.take(2).forEach { ev ->
                    val evPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = Color.WHITE
                        textSize = 24f
                    }
                    canvas.drawText("• ${ev.timeRangeFormatted}: ${ev.title}", marginHorizontal + 36f, currentY, evPaint)
                    currentY += 34f
                }
            }

        } else {
            // BỐ CỤC 2 & 3: 7 CỘT TỐI GIẢN HOẶC KÍNH MỜ
            val colWidth = (cardRight - marginHorizontal - 40f) / 7f
            var startX = marginHorizontal + 20f
            val headerY = cardTop + 50f

            val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = accentColor
                textSize = 34f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val titleStr = if (config.layoutType == CalendarLayoutType.FROSTED) "FROSTED GLASS CALENDAR" else "WEEKLY OVERVIEW"
            canvas.drawText(titleStr, marginHorizontal + 36f, headerY, headerPaint)

            val gridTop = headerY + 40f
            weekDays.forEachIndexed { idx, day ->
                val isToday = day == today
                val colRect = RectF(startX, gridTop, startX + colWidth - 8f, gridTop + 540f)

                val colBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = if (isToday) Color.argb(60, 46, 148, 255) else Color.argb(25, 255, 255, 255)
                }
                canvas.drawRoundRect(colRect, 18f, 18f, colBgPaint)

                // Header Cột
                val dayNamePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = if (isToday) accentColor else Color.argb(200, 255, 255, 255)
                    textSize = 22f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    textAlign = Paint.Align.CENTER
                }
                canvas.drawText(shortDays[idx], colRect.centerX(), gridTop + 36f, dayNamePaint)

                val dayNumPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = if (isToday) accentColor else Color.WHITE
                    textSize = 28f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    textAlign = Paint.Align.CENTER
                }
                canvas.drawText(day.dayOfMonth.toString(), colRect.centerX(), gridTop + 72f, dayNumPaint)

                // Events
                val dayEvents = events.filter { it.occurs(day) }.sortedBy { it.startDate }
                var evY = gridTop + 95f
                dayEvents.take(4).forEach { ev ->
                    val evRect = RectF(startX + 4f, evY, startX + colWidth - 12f, evY + 85f)
                    val evBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        val cat = ev.category.composeColor
                        color = Color.argb(220, (cat.red * 255).toInt(), (cat.green * 255).toInt(), (cat.blue * 255).toInt())
                    }
                    canvas.drawRoundRect(evRect, 10f, 10f, evBgPaint)

                    val evTimePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = Color.argb(220, 255, 255, 255)
                        textSize = 15f
                        textAlign = Paint.Align.CENTER
                    }
                    canvas.drawText(ev.startDate.format(DateTimeFormatter.ofPattern("HH:mm")), evRect.centerX(), evY + 26f, evTimePaint)

                    val evTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = Color.WHITE
                        textSize = 17f
                        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                        textAlign = Paint.Align.CENTER
                    }
                    val shortTitle = if (ev.title.length > 6) ev.title.take(5) + "…" else ev.title
                    canvas.drawText(shortTitle, evRect.centerX(), evY + 56f, evTitlePaint)

                    evY += 95f
                }

                startX += colWidth
            }

            // Today summary bên dưới lưới cột
            val todayY = gridTop + 580f
            val todayTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = accentColor
                textSize = 28f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            canvas.drawText("TODAY AGENDA", marginHorizontal + 36f, todayY, todayTitlePaint)

            val todayEvents = events.filter { it.occurs(today) }
            var subY = todayY + 40f
            if (todayEvents.isNotEmpty()) {
                todayEvents.take(3).forEach { ev ->
                    val evPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = Color.WHITE
                        textSize = 24f
                    }
                    canvas.drawText("• ${ev.timeRangeFormatted}  ${ev.title}", marginHorizontal + 36f, subY, evPaint)
                    subY += 34f
                }
            } else {
                val freePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.argb(150, 255, 255, 255)
                    textSize = 22f
                }
                canvas.drawText("✨ Hôm nay thảnh thơi • Không có lịch trình", marginHorizontal + 36f, subY, freePaint)
            }
        }

        // 5. CẢM BIẾN VÂN TAY QUANG HỌC DƯỚI MÀN HÌNH FIND X9
        val fpCenterY = 2460f
        val fpRadius = 52f
        val fpPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(40, 255, 255, 255)
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }
        canvas.drawCircle(SCREEN_WIDTH / 2f, fpCenterY, fpRadius, fpPaint)

        return output
    }
}
