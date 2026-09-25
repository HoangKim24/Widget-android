package com.hoangkim.widget.data

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.hoangkim.widget.model.CalendarEvent
import com.hoangkim.widget.model.EventCategory
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Trình xuất file lịch chuẩn quốc tế (.ics RFC 5545) và sao lưu/khôi phục dữ liệu (.json).
 * Hoạt động tương đương 100% CalendarExportManager trên iOS.
 */
object CalendarExportManager {

    /**
     * Tạo chuỗi định dạng iCalendar RFC 5545 (.ics) từ danh sách sự kiện.
     * Tương thích hoàn toàn với Google Calendar, Apple Calendar, Microsoft Outlook.
     */
    fun generateICS(events: List<CalendarEvent>): String {
        val sb = StringBuilder()
        sb.append("BEGIN:VCALENDAR\r\n")
        sb.append("VERSION:2.0\r\n")
        sb.append("PRODID:-//HoangKim24//LichTuanAndroid//VI\r\n")
        sb.append("CALSCALE:GREGORIAN\r\n")
        sb.append("METHOD:PUBLISH\r\n")
        sb.append("X-WR-CALNAME:Lịch Tuần\r\n")

        val icsDateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'")
        val icsDateOnlyFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
        val nowStr = LocalDateTime.now().format(icsDateTimeFormatter)

        for (ev in events) {
            sb.append("BEGIN:VEVENT\r\n")
            sb.append("UID:${ev.id}\r\n")
            sb.append("DTSTAMP:$nowStr\r\n")

            if (ev.isAllDay) {
                sb.append("DTSTART;VALUE=DATE:${ev.startDate.toLocalDate().format(icsDateOnlyFormatter)}\r\n")
                sb.append("DTEND;VALUE=DATE:${ev.endDate.toLocalDate().plusDays(1).format(icsDateOnlyFormatter)}\r\n")
            } else {
                sb.append("DTSTART:${ev.startDate.format(icsDateTimeFormatter)}\r\n")
                sb.append("DTEND:${ev.endDate.format(icsDateTimeFormatter)}\r\n")
            }

            sb.append("SUMMARY:${escapeICS(ev.title)}\r\n")
            if (ev.location.isNotEmpty()) {
                sb.append("LOCATION:${escapeICS(ev.location)}\r\n")
            }
            sb.append("DESCRIPTION:${escapeICS("Danh mục: ${ev.category.displayName}")}\r\n")

            if (ev.isRecurringWeekly) {
                val until = ev.recurrenceEndDate
                if (until != null) {
                    sb.append("RRULE:FREQ=WEEKLY;UNTIL=${until.format(icsDateOnlyFormatter)}T235959Z\r\n")
                } else {
                    sb.append("RRULE:FREQ=WEEKLY\r\n")
                }
            }

            sb.append("END:VEVENT\r\n")
        }

        sb.append("END:VCALENDAR\r\n")
        return sb.toString()
    }

    /**
     * Tạo file .ics và mở Share Sheet của hệ thống Android.
     */
    fun exportCalendarICS(context: Context, events: List<CalendarEvent>) {
        val icsContent = generateICS(events)
        val file = File(context.cacheDir, "LichTuan.ics")
        file.writeText(icsContent, Charsets.UTF_8)

        shareFile(context, file, "text/calendar", "Chia sẻ file lịch .ics")
    }

    /**
     * Xuất dữ liệu toàn diện dạng JSON và chia sẻ qua Share Sheet.
     */
    fun exportBackupJSON(context: Context, events: List<CalendarEvent>) {
        val root = JSONObject()
        root.put("app", "LichTuan")
        root.put("platform", "Android")
        root.put("version", "2.0")
        root.put("exportedAt", LocalDateTime.now().toString())

        val array = JSONArray()
        for (ev in events) {
            val obj = JSONObject()
            obj.put("id", ev.id)
            obj.put("title", ev.title)
            obj.put("startDate", ev.startDate.toString())
            obj.put("endDate", ev.endDate.toString())
            obj.put("category", ev.category.name)
            obj.put("isAllDay", ev.isAllDay)
            obj.put("isRecurringWeekly", ev.isRecurringWeekly)
            obj.put("recurrenceEndDate", ev.recurrenceEndDate?.toString() ?: "")
            obj.put("hasReminder", ev.hasReminder)
            obj.put("location", ev.location)
            array.put(obj)
        }
        root.put("events", array)

        val file = File(context.cacheDir, "LichTuan_Backup.json")
        file.writeText(root.toString(2), Charsets.UTF_8)

        shareFile(context, file, "application/json", "Sao lưu dữ liệu lịch (.json)")
    }

    /**
     * Khôi phục danh sách sự kiện từ chuỗi JSON sao lưu.
     */
    fun parseBackupJSON(jsonStr: String): List<CalendarEvent> {
        val results = mutableListOf<CalendarEvent>()
        val root = JSONObject(jsonStr)
        val array = root.optJSONArray("events") ?: return results

        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val id = obj.optString("id", java.util.UUID.randomUUID().toString())
            val title = obj.optString("title", "Sự kiện")
            val startStr = obj.optString("startDate")
            val endStr = obj.optString("endDate")
            val catName = obj.optString("category", EventCategory.OTHER.name)
            val isAllDay = obj.optBoolean("isAllDay", false)
            val isRecurring = obj.optBoolean("isRecurringWeekly", false)
            val recEndStr = obj.optString("recurrenceEndDate", "")
            val hasReminder = obj.optBoolean("hasReminder", true)
            val location = obj.optString("location", "")

            val startDate = try {
                LocalDateTime.parse(startStr)
            } catch (e: Exception) {
                LocalDateTime.now()
            }

            val endDate = try {
                LocalDateTime.parse(endStr)
            } catch (e: Exception) {
                startDate.plusHours(1)
            }

            val category = try {
                EventCategory.valueOf(catName)
            } catch (e: Exception) {
                EventCategory.OTHER
            }

            val recurrenceEndDate = if (recEndStr.isNotEmpty()) {
                try { LocalDate.parse(recEndStr) } catch (e: Exception) { null }
            } else null

            results.add(
                CalendarEvent(
                    id = id,
                    title = title,
                    startDate = startDate,
                    endDate = endDate,
                    category = category,
                    isAllDay = isAllDay,
                    isRecurringWeekly = isRecurring,
                    recurrenceEndDate = recurrenceEndDate,
                    hasReminder = hasReminder,
                    location = location
                )
            )
        }
        return results
    }

    private fun shareFile(context: Context, file: File, mimeType: String, title: String) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(intent, title).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }

    private fun escapeICS(text: String): String {
        return text
            .replace("\\", "\\\\")
            .replace(";", "\\;")
            .replace(",", "\\,")
            .replace("\n", "\\n")
    }
}
