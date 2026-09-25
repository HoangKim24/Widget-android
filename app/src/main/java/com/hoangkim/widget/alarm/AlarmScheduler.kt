package com.hoangkim.widget.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.hoangkim.widget.model.CalendarEvent
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Quản lý lên lịch báo thức chính xác từng giây cho lịch trình bằng AlarmManager.
 * Hỗ trợ Android 12+ (Exact Alarm) và tự động tính toán lần đổ chuông tiếp theo cho lịch lặp tuần.
 */
class AlarmScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleEventAlarm(event: CalendarEvent) {
        if (!event.hasReminder) {
            cancelEventAlarm(event)
            return
        }

        val triggerDateTime = getNextTriggerDateTime(event) ?: return
        val triggerEpochMillis = triggerDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        // Không đặt báo thức nếu thời điểm đã qua
        if (triggerEpochMillis <= System.currentTimeMillis()) return

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(AlarmReceiver.EXTRA_EVENT_ID, event.id)
            putExtra(AlarmReceiver.EXTRA_TITLE, event.title)
            putExtra(AlarmReceiver.EXTRA_LOCATION, event.location)
            putExtra(AlarmReceiver.EXTRA_TIME, event.timeRangeFormatted)
            putExtra(AlarmReceiver.EXTRA_IS_EXACT, true)
            putExtra(AlarmReceiver.EXTRA_IS_RECURRING, event.isRecurringWeekly)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            event.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerEpochMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerEpochMillis,
                        pendingIntent
                    )
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerEpochMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerEpochMillis,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            // Trường hợp người dùng chưa cấp quyền báo thức chính xác, fallback dùng set thông thường
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                triggerEpochMillis,
                pendingIntent
            )
        }
    }

    fun cancelEventAlarm(event: CalendarEvent) {
        cancelAlarmById(event.id)
    }

    fun cancelAlarmById(eventId: String) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            eventId.hashCode(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    fun rescheduleAll(events: List<CalendarEvent>) {
        events.forEach { scheduleEventAlarm(it) }
    }

    private fun getNextTriggerDateTime(event: CalendarEvent): LocalDateTime? {
        val now = LocalDateTime.now()
        val eventTime = event.startDate.toLocalTime()

        // Lịch không lặp tuần
        if (!event.isRecurringWeekly) {
            val eventDate = event.startDate.toLocalDate()
            val dt = LocalDateTime.of(eventDate, eventTime)
            return if (dt.isAfter(now)) dt else null
        }

        // Lịch lặp tuần: tìm ngày diễn ra tiếp theo bắt đầu từ hôm nay
        val targetDayOfWeek = event.startDate.dayOfWeek
        var checkDate = now.toLocalDate()

        for (i in 0..14) {
            if (checkDate.dayOfWeek == targetDayOfWeek) {
                val candidateDateTime = LocalDateTime.of(checkDate, eventTime)
                if (candidateDateTime.isAfter(now)) {
                    // Kiểm tra ngày kết thúc lặp
                    event.recurrenceEndDate?.let {
                        if (checkDate.isAfter(it)) return null
                    }
                    return candidateDateTime
                }
            }
            checkDate = checkDate.plusDays(1)
        }
        return null
    }
}
