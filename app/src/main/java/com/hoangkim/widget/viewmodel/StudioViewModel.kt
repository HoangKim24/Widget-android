package com.hoangkim.widget.viewmodel

import android.app.Application
import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import com.hoangkim.widget.model.AccentColorTheme
import com.hoangkim.widget.model.CalendarEvent
import com.hoangkim.widget.model.CalendarLayoutType
import com.hoangkim.widget.model.CalendarPosition
import com.hoangkim.widget.model.WallpaperConfig
import com.hoangkim.widget.model.WallpaperPreset
import com.hoangkim.widget.repository.EventRepository
import com.hoangkim.widget.wallpaper.FindX9WallpaperRenderer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel phụ trách toàn bộ tùy biến hình nền khóa Studio,
 * duy trì cấu hình qua vòng đời màn hình và chịu trách nhiệm xuất/áp dụng hình nền.
 */
class StudioViewModel(application: Application) : AndroidViewModel(application) {
    val repository = EventRepository(application.applicationContext)

    val events: StateFlow<List<CalendarEvent>> = repository.events

    private val _config = MutableStateFlow(repository.loadWallpaperConfig())
    val config: StateFlow<WallpaperConfig> = _config.asStateFlow()

    fun updateConfig(newConfig: WallpaperConfig) {
        _config.value = newConfig
        repository.saveWallpaperConfig(newConfig)
    }

    fun updatePreset(preset: WallpaperPreset) {
        updateConfig(_config.value.copy(preset = preset))
    }

    fun updateLayoutType(layout: CalendarLayoutType) {
        updateConfig(_config.value.copy(layoutType = layout))
    }

    fun updatePosition(position: CalendarPosition) {
        updateConfig(_config.value.copy(position = position))
    }

    fun updateAccentTheme(theme: AccentColorTheme) {
        updateConfig(_config.value.copy(accentTheme = theme, customHex = null))
    }

    fun updateCustomHex(hex: String) {
        updateConfig(_config.value.copy(customHex = hex))
    }

    fun updateFineTuneYOffset(offset: Float) {
        updateConfig(_config.value.copy(fineTuneYOffsetDp = offset))
    }

    fun renderPreview(context: Context): Bitmap {
        val (w, h) = FindX9WallpaperRenderer.getDeviceScreenDimensions(context)
        return FindX9WallpaperRenderer.renderWallpaper(
            baseImage = null,
            events = events.value,
            config = _config.value,
            includeSystemMockUi = true,
            targetWidth = w,
            targetHeight = h
        )
    }

    fun renderForLockscreen(context: Context): Bitmap {
        val (w, h) = FindX9WallpaperRenderer.getDeviceScreenDimensions(context)
        return FindX9WallpaperRenderer.renderWallpaper(
            baseImage = null,
            events = events.value,
            config = _config.value,
            includeSystemMockUi = false,
            targetWidth = w,
            targetHeight = h
        )
    }

    fun applyLockScreenWallpaper(context: Context): Boolean {
        return try {
            val bitmap = renderForLockscreen(context)
            val wallpaperManager = WallpaperManager.getInstance(context)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK)
            } else {
                wallpaperManager.setBitmap(bitmap)
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    fun saveToGallery(context: Context): Uri? {
        val bitmap = renderForLockscreen(context)
        return FindX9WallpaperRenderer.saveWallpaperToGallery(context, bitmap)
    }
}
