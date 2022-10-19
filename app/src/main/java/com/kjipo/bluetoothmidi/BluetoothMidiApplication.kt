package com.kjipo.bluetoothmidi

import android.app.Application
import timber.log.Timber
import timber.log.Timber.Forest.plant


class BluetoothMidiApplication : Application() {

    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainerImpl(this)

        if (BuildConfig.DEBUG) {
            plant(Timber.DebugTree())
        }
    }

}