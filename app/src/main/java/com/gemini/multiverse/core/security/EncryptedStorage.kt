package com.gemini.multiverse.core.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Hardware-backed encrypted key-value storage using AES-256 GCM.
 * Protects Gemini API Keys and Access Licensing Keys from rooted / extraction attacks.
 */
class EncryptedStorage(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences: SharedPreferences = try {
        createEncryptedPrefs(context)
    } catch (e: Exception) {
        // Fallback or recreate in case of corrupted keystore during reinstall
        context.deleteSharedPreferences(PREFS_FILE_NAME)
        createEncryptedPrefs(context)
    }

    private fun createEncryptedPrefs(context: Context): SharedPreferences {
        return EncryptedSharedPreferences.create(
            context,
            PREFS_FILE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun saveString(key: String, value: String) {
        sharedPreferences.edit().putString(key, value).apply()
    }

    fun getString(key: String, defaultValue: String = ""): String {
        return sharedPreferences.getString(key, defaultValue) ?: defaultValue
    }

    fun saveBoolean(key: String, value: Boolean) {
        sharedPreferences.edit().putBoolean(key, value).apply()
    }

    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return sharedPreferences.getBoolean(key, defaultValue)
    }

    fun remove(key: String) {
        sharedPreferences.edit().remove(key).apply()
    }

    fun clear() {
        sharedPreferences.edit().clear().apply()
    }

    companion object {
        private const val PREFS_FILE_NAME = "lia_secure_vault.pref"
        const val KEY_GEMINI_API_KEY = "key_gemini_api_token"
        const val KEY_LICENSE_KEY = "key_lia_access_license"
        const val KEY_SELECTED_LANGUAGE = "key_selected_language"
        const val KEY_SELECTED_PERSONALITY = "key_selected_personality"
        const val KEY_BACKGROUND_SERVICE = "key_background_service"
        const val KEY_ECHO_CANCELLATION = "key_echo_cancellation"
    }
}
