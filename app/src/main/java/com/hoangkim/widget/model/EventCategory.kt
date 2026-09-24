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
    }
}
