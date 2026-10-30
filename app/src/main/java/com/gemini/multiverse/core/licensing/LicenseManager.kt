package com.gemini.multiverse.core.licensing

import com.gemini.multiverse.data.settings.SettingsRepository

/**
 * LicenseManager - Licensing requirements removed per user specification.
 * Full access to all tools, Visual Agent, and Gemini Live features is unlocked by default.
 */
class LicenseManager(
    private val settingsRepository: SettingsRepository? = null
) {
    /**
     * Always returns true. Unrestricted access for all users.
     */
    fun isAccessAuthorized(): Boolean = true

    fun validateKey(key: String): Boolean = true

    fun getDeviceIdentifier(): String = "UNRESTRICTED"
}
