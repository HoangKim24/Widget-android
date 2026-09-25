package com.hoangkim.widget.data

import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import com.hoangkim.widget.model.CalendarEvent
import com.hoangkim.widget.model.EventCategory
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.UUID

/**
 * Trình đồng bộ sự kiện từ Lịch thiết bị (Google Calendar / Device Calendar).
 * Tương đương với DeviceCalendarSyncManager trên iOS EventKit.
 */
object DeviceCalendarSyncManager {

    data class SelectableEvent(
        val id: String = UUID.randomUUID().toString(),
        val event: CalendarEvent,
        var isSelected: Boolean = true
    )

    fun hasCalendarPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.READ_CALENDAR
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Lấy các sự kiện trong tuần được chỉ định từ CalendarContract.Instances.
     */
    fun fetchEventsForWeek(context: Context, mondayDate: LocalDate): List<SelectableEvent> {
        if (!hasCalendarPermission(context)) return emptyList()

        val results = mutableListOf<SelectableEvent>()
        val startMillis = mondayDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endMillis = mondayDate.plusDays(7).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val builder = CalendarContract.Instances.CONTENT_URI.buildUpon()
        ContentUris.appendId(builder, startMillis)
        ContentUris.appendId(builder, endMillis)

        val projection = arrayOf(
            CalendarContract.Instances.EVENT_ID,
            CalendarContract.Instances.TITLE,
            CalendarContract.Instances.BEGIN,
            CalendarContract.Instances.END,
            CalendarContract.Instances.ALL_DAY,
            CalendarContract.Instances.EVENT_LOCATION
        )

        var cursor: Cursor? = null
        try {
            cursor = context.contentResolver.query(
                builder.build(),
                projection,
                null,
                null,
                "${CalendarContract.Instances.BEGIN} ASC"
            )

            cursor?.let {
                val idxTitle = it.getColumnIndex(CalendarContract.Instances.TITLE)
                val idxBegin = it.getColumnIndex(CalendarContract.Instances.BEGIN)
                val idxEnd = it.getColumnIndex(CalendarContract.Instances.END)
                val idxAllDay = it.getColumnIndex(CalendarContract.Instances.ALL_DAY)
                val idxLocation = it.getColumnIndex(CalendarContract.Instances.EVENT_LOCATION)

                while (it.moveToNext()) {
                    val title = if (idxTitle >= 0) it.getString(idxTitle) ?: "Sự kiện" else "Sự kiện"
                    val begin = if (idxBegin >= 0) it.getLong(idxBegin) else startMillis
                    val end = if (idxEnd >= 0) it.getLong(idxEnd) else (begin + 3600000L)
                    val allDay = if (idxAllDay >= 0) it.getInt(idxAllDay) == 1 else false
                    val location = if (idxLocation >= 0) it.getString(idxLocation) ?: "" else ""

                    val startDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(begin), ZoneId.systemDefault())
                    val endDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(end), ZoneId.systemDefault())

                    val calendarEvent = CalendarEvent(
                        id = UUID.randomUUID().toString(),
                        title = title,
                        startDate = startDateTime,
                        endDate = endDateTime,
                        category = EventCategory.infer(title),
                        isAllDay = allDay,
                        location = location
                    )

                    results.add(SelectableEvent(event = calendarEvent, isSelected = true))
                }
            }
        } catch (e: Exception) {
            // Không làm crash app nếu quyền bị thu hồi đột ngột
        } finally {
            cursor?.close()
        }

        return results
    }
}
