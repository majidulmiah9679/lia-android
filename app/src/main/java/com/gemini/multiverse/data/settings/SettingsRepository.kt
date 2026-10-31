package com.gemini.multiverse.data.settings

import kotlinx.coroutines.flow.StateFlow

data class SettingsState(
    val apiKey: String = "",
    val hasApiKey: Boolean = false,
    val licenseKey: String = "",
    val isLicenseValid: Boolean = false,
    val selectedLanguage: String = "bengali", // "bengali", "hinglish", "hindi", "english"
    val selectedPersonalityId: String = "jarvis_female",
    val isEchoCancellationEnabled: Boolean = true,
    val isBackgroundServiceEnabled: Boolean = true,
    val orbStyle: String = "marigold_neon" // "marigold_neon", "cyber_cyan", "aurora"
)

interface SettingsRepository {
    val settingsState: StateFlow<SettingsState>
    suspend fun saveApiKey(apiKey: String)
    suspend fun clearApiKey()
    suspend fun saveLicenseKey(licenseKey: String)
    suspend fun setLanguage(language: String)
    suspend fun setPersonality(personalityId: String)
    suspend fun setEchoCancellation(enabled: Boolean)
    suspend fun setBackgroundService(enabled: Boolean)
    suspend fun setOrbStyle(style: String)
    fun getApiKeySync(): String
}
