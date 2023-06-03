package com.kjipo.bluetoothmidi

import android.content.Context
import androidx.room.Room
import com.kjipo.bluetoothmidi.bluetooth.BluetoothPairing
import com.kjipo.bluetoothmidi.midi.EarTrainer
import com.kjipo.bluetoothmidi.midi.EarTrainerImpl
import com.kjipo.bluetoothmidi.midi.MidiHandler
import com.kjipo.bluetoothmidi.session.MidiSessionRepository
import com.kjipo.bluetoothmidi.session.MidiSessionRepositoryImpl
import com.kjipo.bluetoothmidi.session.SessionDatabase

interface AppContainer {

    val deviceScanner: DeviceScanner

    val bluetoothPairing: BluetoothPairing

    val sessionDatabase: SessionDatabase

    val midiHandler: MidiHandler

    val midiSessionRepository: MidiSessionRepository

    val earTrainer: EarTrainer

    fun destroy()

}


class AppContainerImpl(private val applicationContext: Context) : AppContainer {
    private var internalMidiHandlerReference: MidiHandler? = null

    override val deviceScanner: DeviceScanner by lazy {
        DeviceScanner(applicationContext)
    }

    override val bluetoothPairing: BluetoothPairing by lazy {
        BluetoothPairing(applicationContext)
    }

    override val sessionDatabase: SessionDatabase by lazy {
        Room.databaseBuilder(applicationContext, SessionDatabase::class.java, "session-database")
            // TODO Only here while developing
            .fallbackToDestructiveMigration()
            .build()
    }

    override val midiSessionRepository: MidiSessionRepository by lazy {
        MidiSessionRepositoryImpl(sessionDatabase)
    }

    override val midiHandler: MidiHandler by lazy {
        MidiHandler(applicationContext).also {
            internalMidiHandlerReference = it
        }
    }

    override val earTrainer: EarTrainer by lazy {
        EarTrainerImpl()
    }

    override fun destroy() {
        internalMidiHandlerReference?.close()
    }

}