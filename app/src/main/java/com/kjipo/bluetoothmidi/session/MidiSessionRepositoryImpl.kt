package com.kjipo.bluetoothmidi.session

import android.content.Context
import androidx.room.Room
import com.kjipo.bluetoothmidi.midi.MidiMessage
import java.time.Instant.now

class MidiSessionRepositoryImpl(applicationContext: Context) : MidiSessionRepository {

    private var currentSession: Session? = null

    private val sessionDatabase =
        Room.databaseBuilder(applicationContext, SessionDatabase::class.java, "session-database")
            .build()

    override fun startSession() {
        startNewSession()
    }

    override suspend fun addMessageToSession(translatedMidiMessage: MidiMessage) {
        if (currentSession == null) {
            startNewSession()
        }

        with(translatedMidiMessage) {
            sessionDatabase.sessionDao().addMidiMessage(
                SessionMidiMessage(
                    0,
                    midiCommand.ordinal,
                    midiData,
                    channel,
                    timestamp
                )
            )
        }
    }

    override fun closeSession() {
        TODO("Not yet implemented")
    }

    private fun startNewSession() {
        currentSession = Session(start = now())
    }

}