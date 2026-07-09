package com.ahmad.girum

import android.app.Application
import com.ahmad.girum.core.AppContainer

class GirumApplication : Application() {
    lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        appContainer = AppContainer(this)
    }
}
