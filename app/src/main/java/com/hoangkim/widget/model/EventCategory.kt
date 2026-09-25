package com.hoangkim.widget.model

import androidx.compose.ui.graphics.Color

enum class EventCategory(
    val id: String,
    val displayName: String,
    val symbolName: String,
    val colorHex: String,
    val composeColor: Color
) {
    WORK("work", "Công việc", "briefcase.fill", "#2B82F6", Color(0xFF2B82F6)),
    PERSONAL("personal", "Cá nhân", "person.fill", "#F59E0B", Color(0xFFF59E0B)),
    HEALTH("health", "Sức khỏe", "heart.fill", "#10B981", Color(0xFF10B981)),
    STUDY("study", "Học tập", "book.fill", "#8B5CF6", Color(0xFF8B5CF6)),
    FAMILY("family", "Gia đình", "house.fill", "#EC4899", Color(0xFFEC4899)),
    OTHER("other", "Khác", "ellipsis.circle.fill", "#6B7280", Color(0xFF6B7280));

    companion object {
        fun fromId(id: String): EventCategory = entries.firstOrNull { it.id == id } ?: OTHER

        /**
         * Tự động dự đoán danh mục thông minh dựa trên từ khóa trong tên sự kiện (tiếng Việt & tiếng Anh).
         */
        fun infer(text: String): EventCategory {
            val lower = text.lowercase()

            fun containsAny(keywords: List<String>): Boolean {
                return keywords.any { lower.contains(it) }
            }

            if (containsAny(listOf("học", "study", "thi", "đọc sách", "lớp", "tiếng anh", "ôn", "bài tập", "lecture", "khóa học", "exam", "course"))) {
                return STUDY
            }
            if (containsAny(listOf("gym", "chạy", "bơi", "yoga", "khám", "thuốc", "thể dục", "relax", "spa", "đi dạo", "bác sĩ", "workout", "fitness"))) {
                return HEALTH
            }
            if (containsAny(listOf("gia đình", "mẹ", "bố", "con", "chợ", "siêu thị", "nấu", "family", "vợ", "chồng", "nhà", "dọn dẹp", "đón"))) {
                return FAMILY
            }
            if (containsAny(listOf("họp", "meeting", "đi làm", "làm việc", "công việc", "work", "kpi", "báo cáo", "dự án", "deadline", "công ty", "task", "code", "khách", "call", "phỏng vấn"))) {
                return WORK
            }
            if (containsAny(listOf("cafe", "cà phê", "bạn", "phim", "du lịch", "mua sắm", "shopee", "chill", "ăn trưa", "ăn tối", "quán", "nhậu", "party", "sinh nhật", "đi chơi"))) {
                return PERSONAL
            }

            return OTHER
        }
    }
}
