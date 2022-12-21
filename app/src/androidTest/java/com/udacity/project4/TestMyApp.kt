package com.udacity.project4

import android.app.Application
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin

class TestMyApp: Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {  }
    }

    override fun onTerminate() {
        super.onTerminate()
        stopKoin()
    }
}