package com.hoangkim.widget.repository

import android.content.Context
import android.content.SharedPreferences
import com.hoangkim.widget.model.CalendarEvent
import com.hoangkim.widget.model.EventCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class EventRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("widget_events_prefs", Context.MODE_PRIVATE)
    private val _events = MutableStateFlow<List<CalendarEvent>>(emptyList())
    val events: StateFlow<List<CalendarEvent>> = _events.asStateFlow()

    init {
        loadEvents()
    }

    fun loadEvents() {
        val jsonString = prefs.getString("saved_events_json", null)
        if (jsonString.isNullOrEmpty()) {
            _events.value = generateDefaultSampleEvents()
            saveEvents()
        } else {
            try {
                val list = mutableListOf<CalendarEvent>()
                val array = JSONArray(jsonString)
                val isoFmt = DateTimeFormatter.ISO_LOCAL_DATE_TIME
                val dateFmt = DateTimeFormatter.ISO_LOCAL_DATE

                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        CalendarEvent(
                            id = obj.getString("id"),
                            title = obj.getString("title"),
                            startDate = LocalDateTime.parse(obj.getString("startDate"), isoFmt),
                            endDate = LocalDateTime.parse(obj.getString("endDate"), isoFmt),
                            category = EventCategory.fromId(obj.optString("category", "other")),
                            isAllDay = obj.optBoolean("isAllDay", false),
                            isRecurringWeekly = obj.optBoolean("isRecurringWeekly", false),
                            recurrenceEndDate = if (obj.has("recurrenceEndDate")) LocalDate.parse(obj.getString("recurrenceEndDate"), dateFmt) else null,
                            hasReminder = obj.optBoolean("hasReminder", true),
                            location = obj.optString("location", "")
                        )
                    )
                }
                _events.value = list
            } catch (e: Exception) {
                _events.value = generateDefaultSampleEvents()
            }
        }
    }

    fun addEvent(event: CalendarEvent) {
        val current = _events.value.toMutableList()
        current.add(event)
        _events.value = current
        saveEvents()
    }

    fun removeEvent(id: String) {
        val current = _events.value.toMutableList()
        current.removeAll { it.id == id }
        _events.value = current
        saveEvents()
    }

    fun clearAll() {
        _events.value = emptyList()
        saveEvents()
    }

    private fun saveEvents() {
        val array = JSONArray()
        val isoFmt = DateTimeFormatter.ISO_LOCAL_DATE_TIME
        val dateFmt = DateTimeFormatter.ISO_LOCAL_DATE

        for (e in _events.value) {
            val obj = JSONObject().apply {
                put("id", e.id)
                put("title", e.title)
                put("startDate", e.startDate.format(isoFmt))
                put("endDate", e.endDate.format(isoFmt))
                put("category", e.category.id)
                put("isAllDay", e.isAllDay)
                put("isRecurringWeekly", e.isRecurringWeekly)
                e.recurrenceEndDate?.let { put("recurrenceEndDate", it.format(dateFmt)) }
                put("hasReminder", e.hasReminder)
                put("location", e.location)
            }
            array.put(obj)
        }
        prefs.edit().putString("saved_events_json", array.toString()).apply()
    }

    private fun generateDefaultSampleEvents(): List<CalendarEvent> {
        val today = LocalDate.now()
        val monday = today.minusDays((today.dayOfWeek.value - 1).toLong())

        return listOf(
            CalendarEvent(
                title = "Toán Cao Cấp",
                location = "Phòng A.201",
                startDate = LocalDateTime.of(monday, LocalTime.of(7, 30)),
                endDate = LocalDateTime.of(monday, LocalTime.of(9, 30)),
                category = EventCategory.WORK,
                isRecurringWeekly = true
            ),
            CalendarEvent(
                title = "Anh Văn Chuyên Ngành",
                location = "Phòng B.302",
                startDate = LocalDateTime.of(monday, LocalTime.of(13, 30)),
                endDate = LocalDateTime.of(monday, LocalTime.of(15, 30)),
                category = EventCategory.STUDY,
                isRecurringWeekly = true
            ),
            CalendarEvent(
                title = "Chạy Bộ Sáng",
                location = "Công viên",
                startDate = LocalDateTime.of(monday.plusDays(1), LocalTime.of(6, 0)),
                endDate = LocalDateTime.of(monday.plusDays(1), LocalTime.of(7, 0)),
                category = EventCategory.HEALTH,
                isRecurringWeekly = true
            ),
            CalendarEvent(
                title = "Lập Trình Android",
                location = "Phòng Lab 3",
                startDate = LocalDateTime.of(monday.plusDays(2), LocalTime.of(13, 0)),
                endDate = LocalDateTime.of(monday.plusDays(2), LocalTime.of(16, 0)),
                category = EventCategory.PERSONAL,
                isRecurringWeekly = true
            ),
            CalendarEvent(
                title = "Họp Nhóm Đồ Án",
                location = "Google Meet",
                startDate = LocalDateTime.of(monday.plusDays(2), LocalTime.of(19, 0)),
                endDate = LocalDateTime.of(monday.plusDays(2), LocalTime.of(20, 30)),
                category = EventCategory.FAMILY,
                isRecurringWeekly = true
            ),
            CalendarEvent(
                title = "Báo Cáo Tiến Độ Tuần",
                location = "Văn phòng",
                startDate = LocalDateTime.of(monday.plusDays(4), LocalTime.of(14, 0)),
                endDate = LocalDateTime.of(monday.plusDays(4), LocalTime.of(15, 30)),
                category = EventCategory.WORK,
                isRecurringWeekly = true
            ),
            CalendarEvent(
                title = "Bóng Đá Cuối Tuần",
                location = "Sân bóng cỏ nhân tạo",
                startDate = LocalDateTime.of(monday.plusDays(6), LocalTime.of(17, 0)),
                endDate = LocalDateTime.of(monday.plusDays(6), LocalTime.of(19, 0)),
                category = EventCategory.HEALTH,
                isRecurringWeekly = true
            )
        )
    }
}
