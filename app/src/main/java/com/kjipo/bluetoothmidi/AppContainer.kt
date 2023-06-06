package com.kjipo.bluetoothmidi

import android.bluetooth.BluetoothManager
import android.content.Context
import android.media.midi.MidiManager
import androidx.room.Room
import com.kjipo.bluetoothmidi.midi.EarTrainer
import com.kjipo.bluetoothmidi.midi.EarTrainerImpl
import com.kjipo.bluetoothmidi.midi.MidiHandler
import com.kjipo.bluetoothmidi.session.MidiSessionRepository
import com.kjipo.bluetoothmidi.session.MidiSessionRepositoryImpl
import com.kjipo.bluetoothmidi.session.SessionDatabase

interface AppContainer {

    val deviceScanner: DeviceScanner

    val sessionDatabase: SessionDatabase

//    val midiHandler: MidiHandler

    val midiSessionRepository: MidiSessionRepository

    val earTrainer: EarTrainer

    fun getMidiHandler(): MidiHandler

    fun destroy()

}


class AppContainerImpl(private val applicationContext: Context) : AppContainer {
    private var midiHandler: MidiHandler

    init {
        val bluetoothManager =
            applicationContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val midiManager = applicationContext.getSystemService(Context.MIDI_SERVICE) as MidiManager

        midiHandler = MidiHandler(bluetoothManager, midiManager)
    }

    override val deviceScanner: DeviceScanner by lazy {
        DeviceScanner(applicationContext)
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


    override val earTrainer: EarTrainer by lazy {
        EarTrainerImpl()
    }

    override fun getMidiHandler(): MidiHandler {
        return midiHandler
    }

    override fun destroy() {
        midiHandler.close()
    }

}