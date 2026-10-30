package com.gemini.multiverse.core.di

import android.content.Context
import com.gemini.multiverse.core.common.DefaultDispatcherProvider
import com.gemini.multiverse.core.common.DispatcherProvider
import com.gemini.multiverse.core.security.EncryptedStorage
import com.gemini.multiverse.data.settings.DefaultSettingsRepository
import com.gemini.multiverse.data.settings.SettingsRepository

/**
 * Dependency Injection container interface for the application.
 */
interface AppContainer {
    val context: Context
    val dispatcherProvider: DispatcherProvider
    val encryptedStorage: EncryptedStorage
    val settingsRepository: SettingsRepository
}

/**
 * Default implementation of the AppContainer.
 */
class DefaultAppContainer(
    override val context: Context
) : AppContainer {
    override val dispatcherProvider: DispatcherProvider by lazy {
        DefaultDispatcherProvider()
    }

    override val encryptedStorage: EncryptedStorage by lazy {
        EncryptedStorage(context)
    }

    override val settingsRepository: SettingsRepository by lazy {
        DefaultSettingsRepository(encryptedStorage)
    }
}
