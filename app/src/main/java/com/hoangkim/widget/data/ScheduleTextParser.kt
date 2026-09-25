package com.hoangkim.widget.data

import com.hoangkim.widget.model.CalendarEvent
import com.hoangkim.widget.model.EventCategory
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.UUID

/**
 * Bộ phân tích cú pháp văn bản lịch trình thông minh (AI Prompt-to-Calendar Engine).
 * Hỗ trợ bóc tách câu nói tự nhiên tiếng Việt, đa ngày (2-4-6, 3-5-7), các buổi trong ngày (sáng, chiều, tối),
 * tin nhắn Zalo, Messenger, Ghi chú và thời khóa biểu quét từ Camera OCR.
 */
object ScheduleTextParser {

    data class ParsedItem(
        val id: String = UUID.randomUUID().toString(),
        var dayOffset: Int, // 0 = T2, 1 = T3, ..., 6 = CN
        var dayName: String,
        var startHour: Int,
        var startMinute: Int,
        var endHour: Int,
        var endMinute: Int,
        var title: String,
        var category: EventCategory,
        var isAllDay: Boolean,
        var isSelected: Boolean = true
    ) {
        val timeFormatted: String
            get() = if (isAllDay) "Cả ngày" else String.format("%02d:%02d - %02d:%02d", startHour, startMinute, endHour, endMinute)

        fun toCalendarEvent(mondayDate: LocalDate, isRecurring: Boolean = false, hasReminder: Boolean = true): CalendarEvent {
            val eventDate = mondayDate.plusDays(dayOffset.toLong())
            val startTime = if (isAllDay) LocalTime.of(0, 0) else LocalTime.of(startHour, startMinute)
            val endTime = if (isAllDay) LocalTime.of(23, 59) else LocalTime.of(endHour, endMinute)

            return CalendarEvent(
                id = id,
                title = title,
                startDate = LocalDateTime.of(eventDate, startTime),
                endDate = LocalDateTime.of(eventDate, endTime),
                category = category,
                isAllDay = isAllDay,
                isRecurringWeekly = isRecurring,
                hasReminder = hasReminder
            )
        }
    }

    private data class ExtractedDetails(
        val sH: Int,
        val sM: Int,
        val eH: Int,
        val eM: Int,
        val title: String,
        val isAllDay: Boolean
    )

    fun vietnameseWeekdayName(dayOffset: Int): String {
        return when (dayOffset) {
            0 -> "Thứ Hai"
            1 -> "Thứ Ba"
            2 -> "Thứ Tư"
            3 -> "Thứ Năm"
            4 -> "Thứ Sáu"
            5 -> "Thứ Bảy"
            6 -> "Chủ Nhật"
            else -> "Thứ Hai"
        }
    }

    /**
     * Phân tích văn bản tự do thành danh sách các mục sự kiện.
     */
    fun parse(text: String): List<ParsedItem> {
        val results = mutableListOf<ParsedItem>()
        val lines = text.lines()
        var lastEncounteredDays = listOf<Int>()

        for (line in lines) {
            val trimmedLine = line.trim()
            if (trimmedLine.isEmpty()) continue

            val detectedDays = detectDaysOfWeek(trimmedLine)
            if (detectedDays.isNotEmpty()) {
                lastEncounteredDays = detectedDays
                if (isDayHeaderOnly(trimmedLine)) {
                    continue
                }
            }

            // Tách theo dấu phẩy hoặc chấm phẩy nếu có nhiều việc trên cùng dòng (bảo vệ cụm số như 2,4,6)
            var normalized = trimmedLine.replace(Regex("(?<=\\d),\\s*(?=\\d)"), "_")
            val parts = normalized.split(';', ',')

            for (part in parts) {
                val restored = part.replace("_", ",").trim()
                if (restored.isEmpty()) continue

                val partDays = detectDaysOfWeek(restored)
                if (partDays.isNotEmpty()) {
                    lastEncounteredDays = partDays
                }
                if (isDayHeaderOnly(restored)) continue

                val targetDays = if (partDays.isNotEmpty()) partDays else lastEncounteredDays
                if (targetDays.isEmpty()) continue

                val details = extractTimeAndTitle(restored)
                if (details != null) {
                    for (day in targetDays) {
                        results.add(
                            ParsedItem(
                                dayOffset = day,
                                dayName = vietnameseWeekdayName(day),
                                startHour = details.sH,
                                startMinute = details.sM,
                                endHour = details.eH,
                                endMinute = details.eM,
                                title = details.title,
                                category = EventCategory.infer(details.title),
                                isAllDay = details.isAllDay
                            )
                        )
                    }
                }
            }
        }

        // Sắp xếp theo thứ tự ngày trong tuần và giờ bắt đầu
        return results.sortedWith(
            compareBy({ it.dayOffset }, { it.startHour }, { it.startMinute })
        )
    }

    // 1. Nhận diện nhóm ngày
    fun detectDaysOfWeek(text: String): List<Int> {
        val lower = text.lowercase()

        // Nhóm 2-4-6
        if (lower.contains("2-4-6") || lower.contains("2,4,6") || lower.contains("2 4 6") ||
            lower.contains("2, 4, 6") || lower.contains("thứ 2, 4, 6") || lower.contains("thu 2, 4, 6") ||
            lower.contains("t2, t4, t6") || lower.contains("t2-t4-t6")
        ) {
            return listOf(0, 2, 4)
        }

        // Nhóm 3-5-7
        if (lower.contains("3-5-7") || lower.contains("3,5,7") || lower.contains("3 5 7") ||
            lower.contains("3, 5, 7") || lower.contains("thứ 3, 5, 7") || lower.contains("thu 3, 5, 7") ||
            lower.contains("t3, t5, t7") || lower.contains("t3-t5-t7")
        ) {
            return listOf(1, 3, 5)
        }

        // Nhóm Cuối tuần
        if (lower.contains("cuối tuần") || lower.contains("cuoi tuan") ||
            lower.contains("t7, cn") || lower.contains("t7-cn") || lower.contains("t7 và cn")
        ) {
            return listOf(5, 6)
        }

        // Nhóm Trong tuần (T2 đến T6)
        if (lower.contains("trong tuần") || lower.contains("từ thứ 2 đến thứ 6") || lower.contains("t2-t6")) {
            return listOf(0, 1, 2, 3, 4)
        }

        // Từng ngày đơn lẻ
        val singleDays = mutableListOf<Int>()
        if (matchesDayKeywords(lower, listOf("thứ 2", "thứ hai", "thu 2", "thu hai", "t2", "monday", "mon"))) singleDays.add(0)
        if (matchesDayKeywords(lower, listOf("thứ 3", "thứ ba", "thu 3", "thu ba", "t3", "tuesday", "tue"))) singleDays.add(1)
        if (matchesDayKeywords(lower, listOf("thứ 4", "thứ tư", "thu 4", "thu tu", "t4", "wednesday", "wed"))) singleDays.add(2)
        if (matchesDayKeywords(lower, listOf("thứ 5", "thứ năm", "thu 5", "thu nam", "t5", "thursday"))) singleDays.add(3)
        if (matchesDayKeywords(lower, listOf("thứ 6", "thứ sáu", "thu 6", "thu sau", "t6", "friday", "fri"))) singleDays.add(4)
        if (matchesDayKeywords(lower, listOf("thứ 7", "thứ bảy", "thu 7", "thu bay", "t7", "saturday", "sat"))) singleDays.add(5)
        if (matchesDayKeywords(lower, listOf("chủ nhật", "chu nhat", "cn", "sunday", "sun"))) singleDays.add(6)

        return singleDays
    }

    private fun matchesDayKeywords(text: String, keywords: List<String>): Boolean {
        for (kw in keywords) {
            if (kw.contains(" ")) {
                if (text.contains(kw)) return true
            } else {
                val pattern = Regex("(^|[^\\p{L}\\p{N}])${Regex.escape(kw)}($|[^\\p{L}\\p{N}])", RegexOption.IGNORE_CASE)
                if (pattern.containsMatchIn(text)) return true
            }
        }
        return false
    }

    // 2. Bóc tách khung giờ & tiêu đề
    private fun extractTimeAndTitle(text: String): ExtractedDetails? {
        val lower = text.lowercase()

        // 1. Trường hợp cả ngày
        if (lower.contains("cả ngày") || lower.contains("all day")) {
            val cleaned = stripDayAndPeriodWords(text)
            val finalTitle = cleanTitle(
                cleaned.replace(Regex("(?i)cả ngày"), "")
                    .replace(Regex("(?i)all day"), "")
            )
            return ExtractedDetails(
                sH = 8, sM = 0, eH = 18, eM = 0,
                title = if (finalTitle.isEmpty()) "Lịch cả ngày" else finalTitle,
                isAllDay = true
            )
        }

        // 2. Mẫu khung giờ khoảng: 08:00 - 10:00, 8h-10h, 8h30 - 10h15, 6h đến 7h, 6g - 7g
        val rangeRegex = Regex("""(?:từ\s+)?(\b\d{1,2})(?:[hHgG](\d{2})?|:(\d{2}))?\s*(?:[-–—~]|đến|tới|to)\s*(\b\d{1,2})(?:[hHgG](\d{2})?|:(\d{2}))""", RegexOption.IGNORE_CASE)
        val rangeMatch = rangeRegex.find(text)
        if (rangeMatch != null) {
            val g = rangeMatch.groups
            var sH = g[1]?.value?.toIntOrNull() ?: 8
            val sM = g[2]?.value?.toIntOrNull() ?: g[3]?.value?.toIntOrNull() ?: 0
            var eH = g[4]?.value?.toIntOrNull() ?: (sH + 1)
            val eM = g[5]?.value?.toIntOrNull() ?: g[6]?.value?.toIntOrNull() ?: 0

            sH = adjustForPeriod(sH, lower)
            eH = adjustForPeriod(eH, lower)

            if (eH < sH || (eH == sH && eM <= sM)) {
                eH = minOf(23, sH + 1)
            }

            val titlePart = text.replaceRange(rangeMatch.range, "")
            val finalTitle = cleanTitle(stripDayAndPeriodWords(titlePart))
            if (finalTitle.isEmpty()) return null

            return ExtractedDetails(sH = sH, sM = sM, eH = eH, eM = eM, title = finalTitle, isAllDay = false)
        }

        // 3. Mẫu một mốc giờ duy nhất: "19h", "lúc 7h", "19:30", "8h sáng"
        val singleRegex = Regex("""(?:lúc\s+|vào\s+)?(?:\b(\d{1,2})[hHgG](\d{2})?|\b(\d{1,2}):(\d{2})\b)""", RegexOption.IGNORE_CASE)
        val singleMatch = singleRegex.find(text)
        if (singleMatch != null) {
            val g = singleMatch.groups
            var sH = 8
            var sM = 0

            if (g[1] != null) {
                sH = g[1]?.value?.toIntOrNull() ?: 8
                sM = g[2]?.value?.toIntOrNull() ?: 0
            } else if (g[3] != null) {
                sH = g[3]?.value?.toIntOrNull() ?: 8
                sM = g[4]?.value?.toIntOrNull() ?: 0
            }

            sH = adjustForPeriod(sH, lower)
            val eH = minOf(23, sH + 1)

            val titlePart = text.replaceRange(singleMatch.range, "")
            val finalTitle = cleanTitle(stripDayAndPeriodWords(titlePart))
            if (finalTitle.isEmpty()) return null

            return ExtractedDetails(sH = sH, sM = sM, eH = eH, eM = sM, title = finalTitle, isAllDay = false)
        }

        // 4. Mẫu buổi tự nhiên không kèm giờ: "sáng", "chiều", "tối"
        if (lower.contains("sáng") || lower.contains("sang")) {
            val finalTitle = cleanTitle(stripDayAndPeriodWords(text))
            if (finalTitle.isNotEmpty()) {
                return ExtractedDetails(sH = 8, sM = 0, eH = 10, eM = 0, title = finalTitle, isAllDay = false)
            }
        }
        if (lower.contains("chiều") || lower.contains("chieu")) {
            val finalTitle = cleanTitle(stripDayAndPeriodWords(text))
            if (finalTitle.isNotEmpty()) {
                return ExtractedDetails(sH = 14, sM = 0, eH = 16, eM = 0, title = finalTitle, isAllDay = false)
            }
        }
        if (lower.contains("tối") || lower.contains("toi")) {
            val finalTitle = cleanTitle(stripDayAndPeriodWords(text))
            if (finalTitle.isNotEmpty()) {
                return ExtractedDetails(sH = 19, sM = 0, eH = 21, eM = 0, title = finalTitle, isAllDay = false)
            }
        }

        return null
    }

    private fun isDayHeaderOnly(text: String): Boolean {
        val cleaned = text.lowercase().replace(Regex("[:-–—*•;, \\t\\r\\n]"), "")
        val dayHeaders = setOf(
            "thứ2", "thứhai", "thứ3", "thứba", "thứ4", "thứtư", "thứ5", "thứnăm", "thứ6", "thứsáu", "thứ7", "thứbảy", "chủnhật",
            "thu2", "thuhai", "thu3", "thuba", "thu4", "thutu", "thu5", "thunam", "thu6", "thusau", "thu7", "thubay", "chunhat",
            "t2", "t3", "t4", "t5", "t6", "t7", "cn"
        )
        return dayHeaders.contains(cleaned)
    }

    private fun adjustForPeriod(hour: Int, text: String): Int {
        var h = hour
        if (h < 12) {
            if (text.contains("tối") || text.contains("toi")) {
                if (h in 6..11) h += 12
            }
            if (text.contains("chiều") || text.contains("chieu")) {
                if (h in 1..6) h += 12
            }
        }
        return minOf(23, maxOf(0, h))
    }

    private fun stripDayAndPeriodWords(text: String): String {
        var s = text
        val removePatterns = listOf(
            Regex("""(?i)\b(thứ\s+[2-7]|thứ\s+hai|thứ\s+ba|thứ\s+tư|thứ\s+năm|thứ\s+sáu|thứ\s+bảy|chủ\s+nhật)\b"""),
            Regex("""(?i)\b(thu\s+[2-7]|thu\s+hai|thu\s+ba|thu\s+tu|thu\s+nam|thu\s+sau|thu\s+bay|chu\s+nhat)\b"""),
            Regex("""(?i)\b(t[2-7]|cn)\b"""),
            Regex("""(?i)\b(2-4-6|3-5-7|2,4,6|3,5,7|2, 4, 6|3, 5, 7)\b"""),
            Regex("""(?i)\b(tuần\s+này|tuan\s+nay)\b""")
        )

        for (pat in removePatterns) {
            s = s.replace(pat, " ")
        }

        s = s.replace(Regex("""(?i)^(sáng|chiều|tối|buổi\s+sáng|buổi\s+chiều|buổi\s+tối|lúc|vào)\s+"""), "")
        s = s.replace(Regex("""(?i)\s+(buổi\s+sáng|buổi\s+chiều|buổi\s+tối|hằng\s+ngày|hàng\s+ngày)$"""), "")
        return s
    }

    private fun cleanTitle(text: String): String {
        var s = text.trim()
        val removeChars = charArrayOf(':', '-', '–', '—', '*', '•', ';', ',', ' ')
        s = s.trim { it in removeChars }
        if (s.isNotEmpty()) {
            s = s.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
        return s
    }
}
