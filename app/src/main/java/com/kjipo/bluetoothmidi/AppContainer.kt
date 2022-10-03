package com.kjipo.bluetoothmidi

import android.content.Context

interface AppContainer {
   val deviceScanner: DeviceScanner
}


class AppContainerImpl(private val applicationContext: Context): AppContainer {
    override val deviceScanner: DeviceScanner by lazy {
        DeviceScanner()
    }

}