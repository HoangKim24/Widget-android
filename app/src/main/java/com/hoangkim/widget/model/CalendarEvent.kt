package com.hoangkim.widget.model

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.UUID

data class CalendarEvent(
    val id: String = UUID.randomUUID().toString(),
    var title: String,
    var startDate: LocalDateTime,
    var endDate: LocalDateTime,
    var category: EventCategory = EventCategory.OTHER,
    var isAllDay: Boolean = false,
    var isRecurringWeekly: Boolean = false,
    var recurrenceEndDate: LocalDate? = null,
    var hasReminder: Boolean = true,
    var location: String = ""
) {
    val durationMinutes: Long
        get() = java.time.Duration.between(startDate, endDate).toMinutes()

    val timeRangeFormatted: String
        get() {
            if (isAllDay) return "Cả ngày"
            val fmt = DateTimeFormatter.ofPattern("HH:mm")
            return "${startDate.format(fmt)} - ${endDate.format(fmt)}"
        }

    fun occurs(on: LocalDate): Boolean {
        val eventStart = startDate.toLocalDate()
        val eventEnd = endDate.toLocalDate()

        if (!isRecurringWeekly) {
            return !on.isBefore(eventStart) && !on.isAfter(eventEnd)
        }

        // Lặp lại hàng tuần:
        // Không diễn ra trước ngày bắt đầu
        if (on.isBefore(eventStart)) return false

        // Khóa ngày dừng lặp nếu có
        recurrenceEndDate?.let {
            if (on.isAfter(it)) return false
        }

        // Kiểm tra cùng thứ trong tuần
        return on.dayOfWeek == eventStart.dayOfWeek
    }

    fun isHappeningNow(): Boolean {
        val now = LocalDateTime.now()
        val today = now.toLocalDate()
        if (!occurs(today)) return false
        if (isAllDay) return true

        val currentMinutes = now.hour * 60 + now.minute
        val startMinutes = startDate.hour * 60 + startDate.minute
        val endMinutes = endDate.hour * 60 + endDate.minute
        return currentMinutes in startMinutes until endMinutes
    }
}
