package com.example.weasel.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weasel.repository.SettingsRepository
import com.example.weasel.repository.ThemeSetting
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ThemeViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val theme: StateFlow<ThemeSetting> = settingsRepository.theme
    val isNavBarTranslucent: StateFlow<Boolean> = settingsRepository.isNavBarTranslucent

    fun setTheme(theme: ThemeSetting) {
        viewModelScope.launch {
            settingsRepository.setTheme(theme)
        }
    }

    fun setNavBarTranslucency(isTranslucent: Boolean) {
        viewModelScope.launch {
            settingsRepository.setNavBarTranslucency(isTranslucent)
        }
    }
}