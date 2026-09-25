package com.hoangkim.widget.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.hoangkim.widget.model.CalendarEvent
import com.hoangkim.widget.model.EventCategory
import org.json.JSONArray
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.UUID

/**
 * Quản trị cơ sở dữ liệu SQLite cục bộ bền vững cho Lịch Tuần.
 * Đảm bảo tính toàn vẹn dữ liệu, chống crash do parse chuỗi JSON và tự động di chuyển dữ liệu cũ.
 */
class CalendarDatabaseHelper(private val context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "lichtuan_database.db"
        const val DATABASE_VERSION = 1

        const val TABLE_EVENTS = "events"
        const val COL_ID = "id"
        const val COL_TITLE = "title"
        const val COL_LOCATION = "location"
        const val COL_START_DATE = "start_date"
        const val COL_END_DATE = "end_date"
        const val COL_CATEGORY = "category"
        const val COL_IS_ALL_DAY = "is_all_day"
        const val COL_IS_RECURRING = "is_recurring_weekly"
        const val COL_RECURRENCE_END = "recurrence_end_date"
        const val COL_HAS_REMINDER = "has_reminder"

        private val ISO_FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME
        private val DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableSql = """
            CREATE TABLE $TABLE_EVENTS (
                $COL_ID TEXT PRIMARY KEY,
                $COL_TITLE TEXT NOT NULL,
                $COL_LOCATION TEXT NOT NULL DEFAULT '',
                $COL_START_DATE TEXT NOT NULL,
                $COL_END_DATE TEXT NOT NULL,
                $COL_CATEGORY TEXT NOT NULL,
                $COL_IS_ALL_DAY INTEGER NOT NULL DEFAULT 0,
                $COL_IS_RECURRING INTEGER NOT NULL DEFAULT 0,
                $COL_RECURRENCE_END TEXT,
                $COL_HAS_REMINDER INTEGER NOT NULL DEFAULT 1
            )
        """.trimIndent()
        db.execSQL(createTableSql)
        db.execSQL("CREATE INDEX idx_start_date ON $TABLE_EVENTS ($COL_START_DATE)")

        // Tự động di chuyển dữ liệu cũ từ SharedPreferences sang SQLite nếu có
        migrateFromPreferencesIfAny(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Xử lý migration cho các phiên bản tiếp theo
    }

    fun getAllEvents(): List<CalendarEvent> {
        val list = mutableListOf<CalendarEvent>()
        val db = readableDatabase
        val cursor = db.query(TABLE_EVENTS, null, null, null, null, null, "$COL_START_DATE ASC")

        cursor.use {
            val idxId = it.getColumnIndexOrThrow(COL_ID)
            val idxTitle = it.getColumnIndexOrThrow(COL_TITLE)
            val idxLocation = it.getColumnIndexOrThrow(COL_LOCATION)
            val idxStart = it.getColumnIndexOrThrow(COL_START_DATE)
            val idxEnd = it.getColumnIndexOrThrow(COL_END_DATE)
            val idxCategory = it.getColumnIndexOrThrow(COL_CATEGORY)
            val idxAllDay = it.getColumnIndexOrThrow(COL_IS_ALL_DAY)
            val idxRecurring = it.getColumnIndexOrThrow(COL_IS_RECURRING)
            val idxRecurrenceEnd = it.getColumnIndexOrThrow(COL_RECURRENCE_END)
            val idxReminder = it.getColumnIndexOrThrow(COL_HAS_REMINDER)

            while (it.moveToNext()) {
                try {
                    val recurrenceEndStr = if (!it.isNull(idxRecurrenceEnd)) it.getString(idxRecurrenceEnd) else null
                    list.add(
                        CalendarEvent(
                            id = it.getString(idxId),
                            title = it.getString(idxTitle),
                            location = it.getString(idxLocation),
                            startDate = LocalDateTime.parse(it.getString(idxStart), ISO_FMT),
                            endDate = LocalDateTime.parse(it.getString(idxEnd), ISO_FMT),
                            category = EventCategory.fromId(it.getString(idxCategory)),
                            isAllDay = it.getInt(idxAllDay) == 1,
                            isRecurringWeekly = it.getInt(idxRecurring) == 1,
                            recurrenceEndDate = recurrenceEndStr?.let { d -> LocalDate.parse(d, DATE_FMT) },
                            hasReminder = it.getInt(idxReminder) == 1
                        )
                    )
                } catch (e: Exception) {
                    // Bỏ qua dòng dữ liệu bị lỗi định dạng ngày tháng để không crash app
                }
            }
        }
        return list
    }

    fun insert(event: CalendarEvent): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_ID, event.id)
            put(COL_TITLE, event.title)
            put(COL_LOCATION, event.location)
            put(COL_START_DATE, event.startDate.format(ISO_FMT))
            put(COL_END_DATE, event.endDate.format(ISO_FMT))
            put(COL_CATEGORY, event.category.id)
            put(COL_IS_ALL_DAY, if (event.isAllDay) 1 else 0)
            put(COL_IS_RECURRING, if (event.isRecurringWeekly) 1 else 0)
            put(COL_RECURRENCE_END, event.recurrenceEndDate?.format(DATE_FMT))
            put(COL_HAS_REMINDER, if (event.hasReminder) 1 else 0)
        }
        return db.insertWithOnConflict(TABLE_EVENTS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun update(event: CalendarEvent): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_TITLE, event.title)
            put(COL_LOCATION, event.location)
            put(COL_START_DATE, event.startDate.format(ISO_FMT))
            put(COL_END_DATE, event.endDate.format(ISO_FMT))
            put(COL_CATEGORY, event.category.id)
            put(COL_IS_ALL_DAY, if (event.isAllDay) 1 else 0)
            put(COL_IS_RECURRING, if (event.isRecurringWeekly) 1 else 0)
            put(COL_RECURRENCE_END, event.recurrenceEndDate?.format(DATE_FMT))
            put(COL_HAS_REMINDER, if (event.hasReminder) 1 else 0)
        }
        return db.update(TABLE_EVENTS, values, "$COL_ID = ?", arrayOf(event.id))
    }

    fun delete(id: String): Int {
        val db = writableDatabase
        return db.delete(TABLE_EVENTS, "$COL_ID = ?", arrayOf(id))
    }

    fun clearAll(): Int {
        val db = writableDatabase
        return db.delete(TABLE_EVENTS, null, null)
    }

    fun insertBatch(events: List<CalendarEvent>) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            for (event in events) {
                val values = ContentValues().apply {
                    put(COL_ID, event.id)
                    put(COL_TITLE, event.title)
                    put(COL_LOCATION, event.location)
                    put(COL_START_DATE, event.startDate.format(ISO_FMT))
                    put(COL_END_DATE, event.endDate.format(ISO_FMT))
                    put(COL_CATEGORY, event.category.id)
                    put(COL_IS_ALL_DAY, if (event.isAllDay) 1 else 0)
                    put(COL_IS_RECURRING, if (event.isRecurringWeekly) 1 else 0)
                    put(COL_RECURRENCE_END, event.recurrenceEndDate?.format(DATE_FMT))
                    put(COL_HAS_REMINDER, if (event.hasReminder) 1 else 0)
                }
                db.insertWithOnConflict(TABLE_EVENTS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    private fun migrateFromPreferencesIfAny(db: SQLiteDatabase) {
        try {
            val prefs = context.getSharedPreferences("widget_events_prefs", Context.MODE_PRIVATE)
            val jsonString = prefs.getString("saved_events_json", null)
            if (!jsonString.isNullOrEmpty()) {
                val array = JSONArray(jsonString)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val values = ContentValues().apply {
                        put(COL_ID, obj.optString("id", UUID.randomUUID().toString()))
                        put(COL_TITLE, obj.getString("title"))
                        put(COL_LOCATION, obj.optString("location", ""))
                        put(COL_START_DATE, obj.getString("startDate"))
                        put(COL_END_DATE, obj.getString("endDate"))
                        put(COL_CATEGORY, obj.optString("category", "other"))
                        put(COL_IS_ALL_DAY, if (obj.optBoolean("isAllDay", false)) 1 else 0)
                        put(COL_IS_RECURRING, if (obj.optBoolean("isRecurringWeekly", false)) 1 else 0)
                        if (obj.has("recurrenceEndDate")) {
                            put(COL_RECURRENCE_END, obj.getString("recurrenceEndDate"))
                        }
                        put(COL_HAS_REMINDER, if (obj.optBoolean("hasReminder", true)) 1 else 0)
                    }
                    db.insertWithOnConflict(TABLE_EVENTS, null, values, SQLiteDatabase.CONFLICT_IGNORE)
                }
            } else {
                // Nếu chưa từng có dữ liệu, tạo mẫu ban đầu
                val defaultEvents = generateDefaultSampleEvents()
                for (event in defaultEvents) {
                    val values = ContentValues().apply {
                        put(COL_ID, event.id)
                        put(COL_TITLE, event.title)
                        put(COL_LOCATION, event.location)
                        put(COL_START_DATE, event.startDate.format(ISO_FMT))
                        put(COL_END_DATE, event.endDate.format(ISO_FMT))
                        put(COL_CATEGORY, event.category.id)
                        put(COL_IS_ALL_DAY, if (event.isAllDay) 1 else 0)
                        put(COL_IS_RECURRING, if (event.isRecurringWeekly) 1 else 0)
                        put(COL_RECURRENCE_END, event.recurrenceEndDate?.format(DATE_FMT))
                        put(COL_HAS_REMINDER, if (event.hasReminder) 1 else 0)
                    }
                    db.insertWithOnConflict(TABLE_EVENTS, null, values, SQLiteDatabase.CONFLICT_IGNORE)
                }
            }
        } catch (e: Exception) {
            // Migration error safely handled
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
