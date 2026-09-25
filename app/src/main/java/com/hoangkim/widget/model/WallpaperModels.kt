package com.hoangkim.widget.model

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// 1. Preset hình nền trẻ trung & sang chảnh (khớp 100% iOS & Web Preview)
enum class WallpaperPreset(
    val id: String,
    val title: String,
    val colors: List<Color>
) {
    SUNSET(
        id = "sunset",
        title = "Hoàng Hôn Chill",
        colors = listOf(Color(0xFF24143D), Color(0xFFD85A5A), Color(0xFFF4A261))
    ),
    AURORA(
        id = "aurora",
        title = "Cực Quang",
        colors = listOf(Color(0xFF0D1438), Color(0xFF2E6B94), Color(0xFF7340B2))
    ),
    OBSIDIAN(
        id = "obsidian",
        title = "Đen OLED",
        colors = listOf(Color(0xFF08080A), Color(0xFF1A1C24), Color(0xFF0F1117))
    ),
    MATCHA(
        id = "matcha",
        title = "Matcha Dịu Êm",
        colors = listOf(Color(0xFF2E4738), Color(0xFF6B8C73), Color(0xFFD1E0CC))
    ),
    CANDY(
        id = "candy",
        title = "Kẹo Ngọt Pastel",
        colors = listOf(Color(0xFFF2B3D9), Color(0xFFBFCCFA), Color(0xFFFAE0CC))
    ),
    CUSTOM(
        id = "custom",
        title = "Ảnh Của Bạn",
        colors = listOf(Color(0xFF08080A), Color(0xFF1A1C24))
    );

    val brush: Brush
        get() = Brush.linearGradient(colors)
}

// 2. Bố cục lịch tuần (khớp 100% 3 kiểu lịch chuẩn iOS)
enum class CalendarLayoutType(
    val id: String,
    val title: String,
    val iconEmoji: String
) {
    ROWS("rows", "7 Dòng Chi Tiết", "📑"),
    COLUMNS("columns", "7 Cột Tối Giản", "📊"),
    FROSTED("frosted", "Kính Mờ", "💎")
}

// 3. Vị trí trên màn hình khóa (khớp 100% mô tả chữ chuẩn iOS)
enum class CalendarPosition(
    val id: String,
    val title: String,
    val yRatio: Float
) {
    TOP("top", "Dưới Đồng Hồ", 0.28f),
    CENTER("center", "Chính Giữa", 0.42f),
    BOTTOM("bottom", "Dưới Đáy", 0.56f)
}

// 4. Màu sắc điểm nhấn & Color Hunt (khớp 100% bảng màu iOS)
enum class AccentColorTheme(
    val id: String,
    val title: String,
    val color: Color,
    val hex: String
) {
    GOLD("gold", "Vàng Gold", Color(0xFFFFD159), "#FFD159"),
    ROSE("rose", "Hồng Neon", Color(0xFFFF73A6), "#FF73A6"),
    CYAN("cyan", "Xanh Băng", Color(0xFF59D9FF), "#59D9FF"),
    MINT("mint", "Xanh Mint", Color(0xFF73F2BF), "#73F2BF"),
    WHITE("white", "Trắng Tinh", Color(0xFFFFFFFF), "#FFFFFF");
}

data class ColorHuntPreset(
    val title: String,
    val hex: String,
    val color: Color
)

object ColorHuntPresets {
    val list = listOf(
        ColorHuntPreset("Cam Đất", "#E76F51", Color(0xFFE76F51)),
        ColorHuntPreset("Xanh Ngọc", "#2A9D8F", Color(0xFF2A9D8F)),
        ColorHuntPreset("Hoàng Hôn", "#E9C46A", Color(0xFFE9C46A)),
        ColorHuntPreset("Tím Pastel", "#B388FF", Color(0xFFB388FF)),
        ColorHuntPreset("Bạc Hà", "#06D6A0", Color(0xFF06D6A0))
    )
}

// 5. Cấu hình Studio Màn Hình Khóa
data class WallpaperConfig(
    var preset: WallpaperPreset = WallpaperPreset.SUNSET,
    var layoutType: CalendarLayoutType = CalendarLayoutType.ROWS,
    var position: CalendarPosition = CalendarPosition.TOP,
    var accentTheme: AccentColorTheme = AccentColorTheme.GOLD,
    var customHex: String? = null,
    var fineTuneYOffsetDp: Float = 0f
) {
    val effectiveColor: Color
        get() {
            val hex = customHex
            if (!hex.isNullOrEmpty()) {
                return try {
                    Color(android.graphics.Color.parseColor(hex))
                } catch (e: Exception) {
                    accentTheme.color
                }
            }
            return accentTheme.color
        }
}

