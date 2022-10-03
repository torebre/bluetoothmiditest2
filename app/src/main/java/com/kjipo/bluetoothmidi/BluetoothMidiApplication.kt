package com.kjipo.bluetoothmidi

import android.app.Application


class BluetoothMidiApplication : Application() {

    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainerImpl(this)
    }

}