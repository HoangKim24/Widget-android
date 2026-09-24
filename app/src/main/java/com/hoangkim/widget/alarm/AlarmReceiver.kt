package com.hoangkim.widget.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("EXTRA_TITLE") ?: "Lịch học / Công việc"
        val location = intent.getStringExtra("EXTRA_LOCATION") ?: ""
        val isExactAlarm = intent.getBooleanExtra("EXTRA_IS_EXACT", false)

        val channelId = "lich_tuan_alarm_channel"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Báo thức Lịch Tuần",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Kênh đổ chuông nhắc nhở và báo thức lịch trình"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(if (isExactAlarm) "⏰ ĐẾN GIỜ: $title" else "🔔 SẮP DIỄN RA: $title")
            .setContentText(if (location.isNotEmpty()) "Địa điểm/Phòng: $location" else "Đã đến khung giờ đã lên lịch.")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setSound(alarmSound)
            .setAutoCancel(true)

        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }
}
