package com.hoangkim.widget.wallpaper

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.hoangkim.widget.model.CalendarEvent

/**
 * Renderer xuất hình nền khóa chuẩn tỷ lệ 19.8:9 cho OPPO Find X9 (1264 x 2780 px).
 * Tự động căn chỉnh lưới lịch tuần rơi trọn vẹn vào "Golden Safe Zone" (26% -> 72% chiều cao),
 * tránh bị che khuất bởi đồng hồ ColorOS ở trên và vân tay/phím tắt ở dưới.
 */
object FindX9WallpaperRenderer {
    const val SCREEN_WIDTH = 1264
    const val SCREEN_HEIGHT = 2780

    // Vùng an toàn màn hình khóa OPPO Find X9
    const val SAFE_ZONE_TOP_RATIO = 0.26f    // Dưới cụm đồng hồ ColorOS
    const val SAFE_ZONE_BOTTOM_RATIO = 0.72f // Trên cảm biến vân tay màn hình
    const val CORNER_RADIUS = 38f

    fun renderWallpaper(
        baseImage: Bitmap?,
        events: List<CalendarEvent>,
        currentDayIndex: Int
    ): Bitmap {
        val output = Bitmap.createBitmap(SCREEN_WIDTH, SCREEN_HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        // 1. Vẽ ảnh nền gốc (hoặc gradient nền tối AMOLED)
        if (baseImage != null) {
            val srcRect = android.graphics.Rect(0, 0, baseImage.width, baseImage.height)
            val dstRect = android.graphics.Rect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT)
            canvas.drawBitmap(baseImage, srcRect, dstRect, null)
            // Lớp phủ tối 40% để làm nổi bật lịch
            canvas.drawColor(Color.argb(100, 0, 0, 0))
        } else {
            // Nền gradient tối sang trọng
            val bgPaint = Paint().apply {
                color = Color.parseColor("#12151B")
            }
            canvas.drawRect(0f, 0f, SCREEN_WIDTH.toFloat(), SCREEN_HEIGHT.toFloat(), bgPaint)
        }

        // 2. Tính toán tọa độ Vùng An Toàn (Golden Safe Zone)
        val safeTop = SCREEN_HEIGHT * SAFE_ZONE_TOP_RATIO
        val safeBottom = SCREEN_HEIGHT * SAFE_ZONE_BOTTOM_RATIO
        val safeHeight = safeBottom - safeTop
        val marginHorizontal = 40f

        // Vẽ Card nền kính mờ chứa lịch tuần
        val cardRect = RectF(
            marginHorizontal,
            safeTop,
            SCREEN_WIDTH - marginHorizontal,
            safeBottom
        )
        val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(180, 30, 34, 43)
        }
        canvas.drawRoundRect(cardRect, 32f, 32f, cardPaint)

        // Viền thẻ kính
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(80, 255, 255, 255)
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }
        canvas.drawRoundRect(cardRect, 32f, 32f, strokePaint)

        // 3. Tiêu đề Bảng Lịch Tuần
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 44f
            isFakeBoldText = true
        }
        canvas.drawText("LỊCH TRÌNH TUẦN NÀY", marginHorizontal + 40f, safeTop + 70f, textPaint)

        return output
    }
}
