package com.gemini.multiverse.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemini.multiverse.data.settings.SettingsRepository
import com.gemini.multiverse.data.settings.SettingsState
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val uiState: StateFlow<SettingsState> = settingsRepository.settingsState

    fun onApiKeySave(apiKey: String) {
        viewModelScope.launch {
            settingsRepository.saveApiKey(apiKey)
        }
    }

    fun onApiKeyClear() {
        viewModelScope.launch {
            settingsRepository.clearApiKey()
        }
    }

    fun onLicenseKeySave(licenseKey: String) {
        viewModelScope.launch {
            settingsRepository.saveLicenseKey(licenseKey)
        }
    }

    fun onLanguageSelect(language: String) {
        viewModelScope.launch {
            settingsRepository.setLanguage(language)
        }
    }

    fun onPersonalitySelect(personalityId: String) {
        viewModelScope.launch {
            settingsRepository.setPersonality(personalityId)
        }
    }

    fun onEchoCancellationToggle(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setEchoCancellation(enabled)
        }
    }

    fun onBackgroundServiceToggle(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setBackgroundService(enabled)
        }
    }

    fun onOrbStyleSelect(style: String) {
        viewModelScope.launch {
            settingsRepository.setOrbStyle(style)
        }
    }
}
