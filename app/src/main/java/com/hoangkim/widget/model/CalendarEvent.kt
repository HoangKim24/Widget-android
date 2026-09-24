package com.hoangkim.widget.model

import java.util.UUID

enum class DayOfWeekEnum(val dayIndex: Int, val shortName: String, val fullName: String) {
    MONDAY(1, "T2", "Thứ Hai"),
    TUESDAY(2, "T3", "Thứ Ba"),
    WEDNESDAY(3, "T4", "Thứ Tư"),
    THURSDAY(4, "T5", "Thứ Năm"),
    FRIDAY(5, "T6", "Thứ Sáu"),
    SATURDAY(6, "T7", "Thứ Bảy"),
    SUNDAY(7, "CN", "Chủ Nhật");

    companion object {
        fun fromIndex(index: Int): DayOfWeekEnum = entries.firstOrNull { it.dayIndex == index } ?: MONDAY
    }
}

data class CalendarEvent(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val location: String = "",
    val dayOfWeek: DayOfWeekEnum,
    val startHour: Int,
    val startMinute: Int = 0,
    val endHour: Int,
    val endMinute: Int = 0,
    val colorHex: String = "#1E88E5",
    val note: String = "",
    val isCompleted: Boolean = false,
    val enableAlarm: Boolean = true
) {
    val durationMinutes: Int
        get() = (endHour * 60 + endMinute) - (startHour * 60 + startMinute)

    val timeRangeFormatted: String
        get() = String.format("%02d:%02d - %02d:%02d", startHour, startMinute, endHour, endMinute)

    fun isHappeningNow(currentDay: DayOfWeekEnum, currentHour: Int, currentMinute: Int): Boolean {
        if (dayOfWeek != currentDay) return false
        val currentTotal = currentHour * 60 + currentMinute
        val startTotal = startHour * 60 + startMinute
        val endTotal = endHour * 60 + endMinute
        return currentTotal in startTotal until endTotal
    }
}
