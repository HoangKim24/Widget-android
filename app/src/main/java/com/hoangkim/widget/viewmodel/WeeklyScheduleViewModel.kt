package com.hoangkim.widget.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hoangkim.widget.model.CalendarEvent
import com.hoangkim.widget.model.EventCategory
import com.hoangkim.widget.repository.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * ViewModel quản lý trạng thái giao diện và tác vụ lịch tuần,
 * sống sót qua quá trình xoay màn hình (configuration changes).
 */
class WeeklyScheduleViewModel(application: Application) : AndroidViewModel(application) {
    val repository = EventRepository(application.applicationContext)

    val events: StateFlow<List<CalendarEvent>> = repository.events

    private val _weekOffset = MutableStateFlow(0)
    val weekOffset: StateFlow<Int> = _weekOffset.asStateFlow()

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    // Danh sách sự kiện được lọc theo ngày đang chọn
    val eventsForSelectedDate: StateFlow<List<CalendarEvent>> = combine(events, _selectedDate) { evList, date ->
        evList.filter { it.occurs(date) }.sortedBy { it.startDate }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setWeekOffset(offset: Int) {
        _weekOffset.value = offset
    }

    fun setSelectedDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun nextWeek() {
        _weekOffset.value += 1
    }

    fun previousWeek() {
        _weekOffset.value -= 1
    }

    fun resetToCurrentWeek() {
        _weekOffset.value = 0
        _selectedDate.value = LocalDate.now()
    }

    fun addEvent(
        title: String,
        location: String,
        startDate: LocalDateTime,
        endDate: LocalDateTime,
        category: EventCategory,
        isRecurringWeekly: Boolean,
        hasReminder: Boolean
    ): Boolean {
        if (title.isBlank()) return false
        val newEvent = CalendarEvent(
            title = title.trim(),
            location = location.trim(),
            startDate = startDate,
            endDate = endDate,
            category = category,
            isRecurringWeekly = isRecurringWeekly,
            hasReminder = hasReminder
        )
        repository.addEvent(newEvent)
        return true
    }

    fun updateEvent(event: CalendarEvent) {
        repository.updateEvent(event)
    }

    fun removeEvent(id: String) {
        repository.removeEvent(id)
    }

    fun clearAll() {
        repository.clearAll()
    }

    fun copyWeekToNextWeek(currentMonday: LocalDate): Int {
        return repository.copyWeekToNextWeek(currentMonday)
    }

    fun restoreFromJson(json: String): Boolean {
        return repository.restoreFromJson(json)
    }

    fun getBackupJson(): String {
        return repository.getBackupJson()
    }

    fun generateIcsString(): String {
        return repository.generateIcsString()
    }
}
