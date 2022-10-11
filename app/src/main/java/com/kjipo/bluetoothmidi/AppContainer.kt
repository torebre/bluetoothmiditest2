package com.kjipo.bluetoothmidi

import android.content.Context
import com.kjipo.bluetoothmidi.bluetooth.BluetoothPairing

interface AppContainer {
    val deviceScanner: DeviceScanner

    val bluetoothPairing: BluetoothPairing
}


class AppContainerImpl(private val applicationContext: Context) : AppContainer {
    override val deviceScanner: DeviceScanner by lazy {
        DeviceScanner(applicationContext)
    }

    override val bluetoothPairing: BluetoothPairing by lazy {
        BluetoothPairing(applicationContext)
    }

}