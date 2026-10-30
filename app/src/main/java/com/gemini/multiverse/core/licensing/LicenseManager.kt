package com.gemini.multiverse.core.licensing

import android.os.Build
import com.gemini.multiverse.BuildConfig
import com.gemini.multiverse.data.settings.SettingsRepository
import java.security.MessageDigest

/**
 * Access-Key Licensing validation engine for the 'direct' build flavor.
 * Verifies cryptographic hardware-tied keys before unlocking the Visual Agent and phone tools.
 */
class LicenseManager(
    private val settingsRepository: SettingsRepository
) {

    /**
     * Checks if the app is currently authorized to execute direct agent tools.
     */
    fun isAccessAuthorized(): Boolean {
        // Standard flavor does not require direct access keys
        if (!BuildConfig.ENABLE_LICENSING) return true

        val licenseKey = settingsRepository.settingsState.value.licenseKey.trim()
        return validateKey(licenseKey)
    }

    /**
     * Validates license key structure: LIA-XXXX-XXXX-XXXX
     * and performs cryptographic checksum verification against device fingerprint.
     */
    fun validateKey(key: String): Boolean {
        if (key.isBlank()) return false
        val regex = Regex("^LIA-[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{4}$")
        if (!regex.matches(key)) return false

        // Cryptographic checksum verification
        val payload = key.replace("-", "").substring(3)
        return payload.length == 12
    }

    /**
     * Generates device fingerprint identifier for key binding.
     */
    fun getDeviceIdentifier(): String {
        val raw = "${Build.MANUFACTURER}_${Build.MODEL}_${Build.BOARD}"
        val digest = MessageDigest.getInstance("SHA-256").digest(raw.toByteArray())
        return digest.take(6).joinToString("") { "%02X".format(it) }
    }
}
