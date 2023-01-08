package com.kjipo.bluetoothmidi

import android.content.Context
import com.kjipo.bluetoothmidi.bluetooth.BluetoothPairing
import com.kjipo.bluetoothmidi.midi.MidiHandler
import com.kjipo.bluetoothmidi.session.MidiSessionRepository
import com.kjipo.bluetoothmidi.session.MidiSessionRepositoryImpl

interface AppContainer {
    val deviceScanner: DeviceScanner

    val bluetoothPairing: BluetoothPairing

    val midiHandler: MidiHandler

    val midiSessionRepository: MidiSessionRepository

}


class AppContainerImpl(private val applicationContext: Context) : AppContainer {
    override val deviceScanner: DeviceScanner by lazy {
        DeviceScanner(applicationContext)
    }

    override val bluetoothPairing: BluetoothPairing by lazy {
        BluetoothPairing(applicationContext)
    }

    override val midiSessionRepository: MidiSessionRepository by lazy {
        MidiSessionRepositoryImpl(applicationContext)
    }

    override val midiHandler: MidiHandler by lazy {
        MidiHandler(applicationContext)
    }


}