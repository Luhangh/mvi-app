package com.pos.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class POSApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // 初始化Timber
        // if (BuildConfig.DEBUG) {
        //     Timber.plant(Timber.DebugTree())
        // }

        Timber.d("POS Application initialized")
    }
}