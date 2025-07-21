// In: app/src/main/java/com/example/weasel/repository/SettingsRepository.kt
package com.example.weasel.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

enum class ThemeSetting {
    SYSTEM, LIGHT, DARK
}

class SettingsRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)

    private val _theme = MutableStateFlow(getTheme())
    val theme: StateFlow<ThemeSetting> = _theme

    private val _isNavBarTranslucent = MutableStateFlow(isNavBarTranslucent())
    val isNavBarTranslucent: StateFlow<Boolean> = _isNavBarTranslucent

    fun getTheme(): ThemeSetting {
        return ThemeSetting.valueOf(
            prefs.getString(KEY_THEME, ThemeSetting.SYSTEM.name) ?: ThemeSetting.SYSTEM.name
        )
    }

    fun setTheme(theme: ThemeSetting) {
        prefs.edit {
            putString(KEY_THEME, theme.name)
        }
        _theme.value = theme
    }

    fun isNavBarTranslucent(): Boolean {
        return prefs.getBoolean(KEY_NAV_BAR_TRANSLUCENT, true)
    }

    fun setNavBarTranslucency(isTranslucent: Boolean) {
        prefs.edit {
            putBoolean(KEY_NAV_BAR_TRANSLUCENT, isTranslucent)
        }
        _isNavBarTranslucent.value = isTranslucent
    }

    companion object {
        private const val KEY_THEME = "key_theme"
        private const val KEY_NAV_BAR_TRANSLUCENT = "key_nav_bar_translucent"
    }
}