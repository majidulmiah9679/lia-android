package com.gemini.multiverse

import android.app.Application
import com.gemini.multiverse.core.di.AppContainer
import com.gemini.multiverse.core.di.DefaultAppContainer

/**
 * Base Application class initializing the DI container and app-wide state.
 */
class GeminiMultiverseApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        container = DefaultAppContainer(applicationContext)
    }

    companion object {
        lateinit var instance: GeminiMultiverseApp
            private set
    }
}
