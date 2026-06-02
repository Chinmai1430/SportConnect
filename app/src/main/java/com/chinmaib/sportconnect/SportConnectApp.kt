package com.chinmaib.sportconnect

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SportConnectApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Hilt initializes the dependency graph here automatically
    }
}