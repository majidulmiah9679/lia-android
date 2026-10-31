package com.gemini.multiverse.data.settings

import com.gemini.multiverse.core.security.EncryptedStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class DefaultSettingsRepository(
    private val encryptedStorage: EncryptedStorage
) : SettingsRepository {

    private val _settingsState = MutableStateFlow(loadInitialState())
    override val settingsState: StateFlow<SettingsState> = _settingsState.asStateFlow()

    private fun loadInitialState(): SettingsState {
        val key = encryptedStorage.getString(EncryptedStorage.KEY_GEMINI_API_KEY, "")
        val license = encryptedStorage.getString(EncryptedStorage.KEY_LICENSE_KEY, "")
        val lang = encryptedStorage.getString(EncryptedStorage.KEY_SELECTED_LANGUAGE, "bengali")
        val persona = encryptedStorage.getString(EncryptedStorage.KEY_SELECTED_PERSONALITY, "jarvis_female")
        val bgService = encryptedStorage.getBoolean(EncryptedStorage.KEY_BACKGROUND_SERVICE, true)
        val echoCancel = encryptedStorage.getBoolean(EncryptedStorage.KEY_ECHO_CANCELLATION, true)

        return SettingsState(
            apiKey = key,
            hasApiKey = key.isNotBlank(),
            licenseKey = license,
            isLicenseValid = validateLicenseFormat(license),
            selectedLanguage = lang,
            selectedPersonalityId = persona,
            isEchoCancellationEnabled = echoCancel,
            isBackgroundServiceEnabled = bgService
        )
    }

    private fun validateLicenseFormat(license: String): Boolean {
        // Direct tier license format: LIA-XXXX-XXXX-XXXX
        if (license.isBlank()) return false
        val regex = Regex("^LIA-[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{4}$")
        return regex.matches(license.trim())
    }

    override suspend fun saveApiKey(apiKey: String) {
        val trimmed = apiKey.trim()
        encryptedStorage.saveString(EncryptedStorage.KEY_GEMINI_API_KEY, trimmed)
        _settingsState.update {
            it.copy(
                apiKey = trimmed,
                hasApiKey = trimmed.isNotBlank()
            )
        }
    }

    override suspend fun clearApiKey() {
        encryptedStorage.remove(EncryptedStorage.KEY_GEMINI_API_KEY)
        _settingsState.update {
            it.copy(
                apiKey = "",
                hasApiKey = false
            )
        }
    }

    override suspend fun saveLicenseKey(licenseKey: String) {
        val trimmed = licenseKey.trim()
        encryptedStorage.saveString(EncryptedStorage.KEY_LICENSE_KEY, trimmed)
        val isValid = validateLicenseFormat(trimmed)
        _settingsState.update {
            it.copy(
                licenseKey = trimmed,
                isLicenseValid = isValid
            )
        }
    }

    override suspend fun setLanguage(language: String) {
        encryptedStorage.saveString(EncryptedStorage.KEY_SELECTED_LANGUAGE, language)
        _settingsState.update { it.copy(selectedLanguage = language) }
    }

    override suspend fun setPersonality(personalityId: String) {
        encryptedStorage.saveString(EncryptedStorage.KEY_SELECTED_PERSONALITY, personalityId)
        _settingsState.update { it.copy(selectedPersonalityId = personalityId) }
    }

    override suspend fun setEchoCancellation(enabled: Boolean) {
        encryptedStorage.saveBoolean(EncryptedStorage.KEY_ECHO_CANCELLATION, enabled)
        _settingsState.update { it.copy(isEchoCancellationEnabled = enabled) }
    }

    override suspend fun setBackgroundService(enabled: Boolean) {
        encryptedStorage.saveBoolean(EncryptedStorage.KEY_BACKGROUND_SERVICE, enabled)
        _settingsState.update { it.copy(isBackgroundServiceEnabled = enabled) }
    }

    override suspend fun setOrbStyle(style: String) {
        _settingsState.update { it.copy(orbStyle = style) }
    }

    override fun getApiKeySync(): String {
        return encryptedStorage.getString(EncryptedStorage.KEY_GEMINI_API_KEY, "")
    }
}
