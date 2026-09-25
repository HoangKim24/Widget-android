package com.hoangkim.widget.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.hoangkim.widget.repository.EventRepository

/**
 * Tự động đăng ký lại toàn bộ báo thức và nhắc nhở sau khi điện thoại khởi động lại.
 */
class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == "android.intent.action.QUICKBOOT_POWERON" ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            val repository = EventRepository(context)
            val events = repository.events.value
            val scheduler = AlarmScheduler(context)
            scheduler.rescheduleAll(events)
        }
    }
}
