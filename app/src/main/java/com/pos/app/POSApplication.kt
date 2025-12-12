package com.pos.app

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class POSApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // 初始化Timber
        if (Log.isLoggable("POS", Log.DEBUG)) {
            Timber.plant(Timber.DebugTree())
        }


        Timber.d("POS Application initialized")
    }
}