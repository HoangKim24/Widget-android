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
import java.util.UUID

import com.hoangkim.widget.alarm.AlarmScheduler
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import com.hoangkim.widget.data.CalendarDatabaseHelper

class EventRepository(context: Context) {
    private val appContext = context.applicationContext
    private val prefs: SharedPreferences = appContext.getSharedPreferences("widget_events_prefs", Context.MODE_PRIVATE)
    private val dbHelper = CalendarDatabaseHelper(appContext)
    private val _events = MutableStateFlow<List<CalendarEvent>>(emptyList())
    val events: StateFlow<List<CalendarEvent>> = _events.asStateFlow()
    private val alarmScheduler = AlarmScheduler(appContext)

    init {
        loadEvents()
        try {
            alarmScheduler.rescheduleAll(_events.value)
        } catch (e: Exception) {
            // Ignore during test/preview
        }
    }

    fun loadEvents() {
        val loaded = dbHelper.getAllEvents()
        if (loaded.isEmpty()) {
            val defaults = generateDefaultSampleEvents()
            dbHelper.insertBatch(defaults)
            _events.value = defaults
        } else {
            _events.value = loaded
        }
        syncBackupJson()
    }

    fun addEvent(event: CalendarEvent) {
        dbHelper.insert(event)
        val current = _events.value.toMutableList()
        val existingIndex = current.indexOfFirst { it.id == event.id }
        if (existingIndex >= 0) {
            current[existingIndex] = event
        } else {
            current.add(event)
        }
        _events.value = current
        syncBackupJson()
        alarmScheduler.scheduleEventAlarm(event)
        notifyWidgetUpdate()
    }

    fun updateEvent(event: CalendarEvent) {
        dbHelper.update(event)
        val current = _events.value.toMutableList()
        val index = current.indexOfFirst { it.id == event.id }
        if (index != -1) {
            current[index] = event
            _events.value = current
            syncBackupJson()
            alarmScheduler.scheduleEventAlarm(event)
            notifyWidgetUpdate()
        }
    }

    fun removeEvent(id: String) {
        dbHelper.delete(id)
        val current = _events.value.toMutableList()
        current.removeAll { it.id == id }
        _events.value = current
        syncBackupJson()
        alarmScheduler.cancelAlarmById(id)
        notifyWidgetUpdate()
    }

    fun clearAll() {
        val oldEvents = _events.value
        dbHelper.clearAll()
        _events.value = emptyList()
        syncBackupJson()
        oldEvents.forEach { alarmScheduler.cancelAlarmById(it.id) }
        notifyWidgetUpdate()
    }

    /**
     * Sao chép toàn bộ lịch tuần nguồn sang 1, 2 hoặc 4 tuần (cả tháng) tiếp theo.
     * Tương ứng với tính năng copyCurrentWeek trên bản iPhone.
     */
    fun copyWeekEvents(sourceWeekMonday: LocalDate, numberOfWeeks: Int): Int {
        val weekEnd = sourceWeekMonday.plusDays(6)
        val sourceEvents = _events.value.filter {
            val d = it.startDate.toLocalDate()
            !d.isBefore(sourceWeekMonday) && !d.isAfter(weekEnd)
        }
        if (sourceEvents.isEmpty()) return 0

        val newEvents = mutableListOf<CalendarEvent>()
        for (w in 1..numberOfWeeks) {
            val daysToAdd = (w * 7).toLong()
            for (ev in sourceEvents) {
                val newStart = ev.startDate.plusDays(daysToAdd)
                val newEnd = ev.endDate.plusDays(daysToAdd)
                val newEv = ev.copy(
                    id = java.util.UUID.randomUUID().toString(),
                    startDate = newStart,
                    endDate = newEnd
                )
                newEvents.add(newEv)
            }
        }

        dbHelper.insertBatch(newEvents)
        val updated = _events.value.toMutableList().apply { addAll(newEvents) }
        _events.value = updated
        syncBackupJson()
        newEvents.forEach { if (it.hasReminder) alarmScheduler.scheduleEventAlarm(it) }
        notifyWidgetUpdate()
        return newEvents.size
    }

    /**
     * Nạp danh sách sự kiện từ file sao lưu JSON hoặc bóc tách OCR/Zalo vào cơ sở dữ liệu.
     */
    fun importEvents(imported: List<CalendarEvent>) {
        if (imported.isEmpty()) return
        dbHelper.insertBatch(imported)
        val current = _events.value.toMutableList()
        imported.forEach { newEv ->
            val idx = current.indexOfFirst { it.id == newEv.id }
            if (idx >= 0) {
                current[idx] = newEv
            } else {
                current.add(newEv)
            }
        }
        _events.value = current
        syncBackupJson()
        imported.forEach { if (it.hasReminder) alarmScheduler.scheduleEventAlarm(it) }
        notifyWidgetUpdate()
    }

    private fun syncBackupJson() {
        try {
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
        } catch (e: Exception) {
            // Backup error ignored
        }
    }

    private fun notifyWidgetUpdate() {
        try {
            CoroutineScope(Dispatchers.IO).launch {
                com.hoangkim.widget.widget.WeeklyScheduleWidget().updateAll(appContext)
            }
        } catch (e: Exception) {
            // Glance widget update error ignored
        }
    }


    fun saveWallpaperConfig(config: com.hoangkim.widget.model.WallpaperConfig) {
        val obj = JSONObject().apply {
            put("preset", config.preset.id)
            put("layoutType", config.layoutType.id)
            put("position", config.position.id)
            put("accentTheme", config.accentTheme.id)
            put("customHex", config.customHex ?: "")
            put("fineTuneYOffsetDp", config.fineTuneYOffsetDp.toDouble())
        }
        prefs.edit().putString("saved_wallpaper_config", obj.toString()).apply()
    }

    fun loadWallpaperConfig(): com.hoangkim.widget.model.WallpaperConfig {
        val jsonStr = prefs.getString("saved_wallpaper_config", null) ?: return com.hoangkim.widget.model.WallpaperConfig()
        return try {
            val obj = JSONObject(jsonStr)
            val presetId = obj.optString("preset", "sunset")
            val layoutId = obj.optString("layoutType", "rows")
            val posId = obj.optString("position", "top")
            val themeId = obj.optString("accentTheme", "gold")
            val customHex = obj.optString("customHex", "").ifEmpty { null }
            val yOffset = obj.optDouble("fineTuneYOffsetDp", 0.0).toFloat()

            com.hoangkim.widget.model.WallpaperConfig(
                preset = com.hoangkim.widget.model.WallpaperPreset.entries.firstOrNull { it.id == presetId } ?: com.hoangkim.widget.model.WallpaperPreset.SUNSET,
                layoutType = com.hoangkim.widget.model.CalendarLayoutType.entries.firstOrNull { it.id == layoutId } ?: com.hoangkim.widget.model.CalendarLayoutType.ROWS,
                position = com.hoangkim.widget.model.CalendarPosition.entries.firstOrNull { it.id == posId } ?: com.hoangkim.widget.model.CalendarPosition.TOP,
                accentTheme = com.hoangkim.widget.model.AccentColorTheme.entries.firstOrNull { it.id == themeId } ?: com.hoangkim.widget.model.AccentColorTheme.GOLD,
                customHex = customHex,
                fineTuneYOffsetDp = yOffset
            )
        } catch (e: Exception) {
            com.hoangkim.widget.model.WallpaperConfig()
        }
    }


    fun copyWeekToNextWeek(currentMonday: LocalDate): Int {
        val nextMonday = currentMonday.plusWeeks(1)
        val currentEvents = _events.value.filter { e ->
            (0..6).any { dayOffset -> e.occurs(currentMonday.plusDays(dayOffset.toLong())) }
        }
        var copiedCount = 0
        val currentList = _events.value.toMutableList()
        for (e in currentEvents) {
            val oldDate = e.startDate.toLocalDate()
            val dayDiff = java.time.temporal.ChronoUnit.DAYS.between(currentMonday, oldDate)
            val targetDate = nextMonday.plusDays(dayDiff)
            val newStart = LocalDateTime.of(targetDate, e.startDate.toLocalTime())
            val newEnd = LocalDateTime.of(targetDate, e.endDate.toLocalTime())

            val exists = currentList.any { it.title == e.title && it.startDate == newStart }
            if (!exists) {
                val newEvent = e.copy(
                    id = UUID.randomUUID().toString(),
                    startDate = newStart,
                    endDate = newEnd,
                    isRecurringWeekly = false
                )
                currentList.add(newEvent)
                dbHelper.insert(newEvent)
                alarmScheduler.scheduleEventAlarm(newEvent)
                copiedCount++
            }
        }
        _events.value = currentList
        syncBackupJson()
        notifyWidgetUpdate()
        return copiedCount
    }

    fun generateIcsString(): String {
        val sb = StringBuilder()
        val utcFormatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss")
        sb.appendLine("BEGIN:VCALENDAR")
        sb.appendLine("VERSION:2.0")
        sb.appendLine("PRODID:-//HoangKim//LichTuan App//VI")
        sb.appendLine("CALSCALE:GREGORIAN")
        sb.appendLine("METHOD:PUBLISH")

        for (e in _events.value) {
            sb.appendLine("BEGIN:VEVENT")
            sb.appendLine("UID:${e.id}@hoangkim.widget")
            sb.appendLine("DTSTAMP:${LocalDateTime.now().format(utcFormatter)}")
            sb.appendLine("DTSTART:${e.startDate.format(utcFormatter)}")
            sb.appendLine("DTEND:${e.endDate.format(utcFormatter)}")
            sb.appendLine("SUMMARY:${e.title}")
            if (e.location.isNotEmpty()) {
                sb.appendLine("LOCATION:${e.location}")
            }
            sb.appendLine("CATEGORIES:${e.category.displayName}")
            if (e.isRecurringWeekly) {
                sb.appendLine("RRULE:FREQ=WEEKLY")
            }
            sb.appendLine("END:VEVENT")
        }
        sb.appendLine("END:VCALENDAR")
        return sb.toString()
    }

    fun getBackupJson(): String {
        return prefs.getString("saved_events_json", "[]") ?: "[]"
    }

    fun restoreFromJson(jsonString: String): Boolean {
        return try {
            val list = mutableListOf<CalendarEvent>()
            val array = JSONArray(jsonString)
            val isoFmt = DateTimeFormatter.ISO_LOCAL_DATE_TIME
            val dateFmt = DateTimeFormatter.ISO_LOCAL_DATE

            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    CalendarEvent(
                        id = obj.optString("id", UUID.randomUUID().toString()),
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
            dbHelper.clearAll()
            dbHelper.insertBatch(list)
            _events.value = list
            syncBackupJson()
            alarmScheduler.rescheduleAll(list)
            notifyWidgetUpdate()
            true
        } catch (e: Exception) {
            false
        }
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
