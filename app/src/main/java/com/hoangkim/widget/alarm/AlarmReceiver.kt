package com.hoangkim.widget.alarm

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.hoangkim.widget.MainActivity
import com.hoangkim.widget.repository.EventRepository

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        const val EXTRA_EVENT_ID = "EXTRA_EVENT_ID"
        const val EXTRA_TITLE = "EXTRA_TITLE"
        const val EXTRA_LOCATION = "EXTRA_LOCATION"
        const val EXTRA_TIME = "EXTRA_TIME"
        const val EXTRA_IS_EXACT = "EXTRA_IS_EXACT"
        const val EXTRA_IS_RECURRING = "EXTRA_IS_RECURRING"
        const val EXTRA_NOTIFICATION_ID = "EXTRA_NOTIFICATION_ID"

        const val ACTION_SNOOZE = "com.hoangkim.widget.ACTION_SNOOZE"
        const val ACTION_DISMISS = "com.hoangkim.widget.ACTION_DISMISS"

        const val CHANNEL_ID = "lich_tuan_alarm_channel_v2"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val action = intent.action

        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, 0)

        // Xử lý nút "Đã xem" (Dismiss)
        if (action == ACTION_DISMISS) {
            notificationManager.cancel(notificationId)
            return
        }

        // Xử lý nút "Nhắc lại sau 5 phút" (Snooze)
        if (action == ACTION_SNOOZE) {
            notificationManager.cancel(notificationId)
            snoozeAlarm(context, intent)
            return
        }

        // Báo thức chính thức đổ chuông
        val eventId = intent.getStringExtra(EXTRA_EVENT_ID) ?: ""
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "Lịch học / Công việc"
        val location = intent.getStringExtra(EXTRA_LOCATION) ?: ""
        val timeRange = intent.getStringExtra(EXTRA_TIME) ?: ""
        val isExactAlarm = intent.getBooleanExtra(EXTRA_IS_EXACT, true)
        val isRecurring = intent.getBooleanExtra(EXTRA_IS_RECURRING, false)

        val uniqueNotifId = (System.currentTimeMillis() % 100000).toInt()

        // 1. Tạo Notification Channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build()

            val channel = NotificationChannel(
                CHANNEL_ID,
                "Báo thức Lịch Tuần OPPO",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Kênh đổ chuông nhắc nhở và báo thức lịch trình học tập, làm việc"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500, 200, 500)
                setSound(alarmSound, audioAttributes)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)
        }

        // 2. Intent mở MainActivity khi chạm vào thông báo
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            uniqueNotifId,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 3. Action "Nhắc lại 5 phút"
        val snoozeIntent = Intent(context, AlarmReceiver::class.java).apply {
            this.action = ACTION_SNOOZE
            putExtras(intent)
            putExtra(EXTRA_NOTIFICATION_ID, uniqueNotifId)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            uniqueNotifId + 1,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 4. Action "Đã hiểu"
        val dismissIntent = Intent(context, AlarmReceiver::class.java).apply {
            this.action = ACTION_DISMISS
            putExtra(EXTRA_NOTIFICATION_ID, uniqueNotifId)
        }
        val dismissPendingIntent = PendingIntent.getBroadcast(
            context,
            uniqueNotifId + 2,
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val subtext = if (location.isNotEmpty()) "📍 $location • Khung giờ: $timeRange" else "Khung giờ: $timeRange"

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(if (isExactAlarm) "⏰ ĐẾN GIỜ: $title" else "🔔 SẮP DIỄN RA: $title")
            .setContentText(subtext)
            .setStyle(NotificationCompat.BigTextStyle().bigText("Đã đến giờ $title.\n$subtext"))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setSound(alarmSound)
            .setVibrate(longArrayOf(0, 500, 200, 500, 200, 500))
            .setContentIntent(openAppPendingIntent)
            .addAction(android.R.drawable.ic_menu_recent_history, "Nhắc lại 5p", snoozePendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Đã hiểu", dismissPendingIntent)
            .setAutoCancel(true)

        notificationManager.notify(uniqueNotifId, builder.build())

        // 5. Nếu là sự kiện lặp tuần, tự động lên lịch cho tuần sau
        if (isRecurring && eventId.isNotEmpty()) {
            val repository = EventRepository(context)
            val event = repository.events.value.firstOrNull { it.id == eventId }
            if (event != null) {
                AlarmScheduler(context).scheduleEventAlarm(event)
            }
        }
    }

    private fun snoozeAlarm(context: Context, originalIntent: Intent) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val snoozeTimeMillis = System.currentTimeMillis() + 5 * 60 * 1000 // 5 phút sau

        val retryIntent = Intent(context, AlarmReceiver::class.java).apply {
            putExtras(originalIntent)
            action = null // Reset action để trở thành báo thức chính
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            (System.currentTimeMillis() % 10000).toInt(),
            retryIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                snoozeTimeMillis,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                snoozeTimeMillis,
                pendingIntent
            )
        }
    }
}
