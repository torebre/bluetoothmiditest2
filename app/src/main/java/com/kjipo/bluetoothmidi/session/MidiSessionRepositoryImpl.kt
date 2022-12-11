package com.kjipo.bluetoothmidi.session

import android.content.Context
import androidx.room.Room
import com.kjipo.bluetoothmidi.midi.MidiMessage
import java.time.Instant.now

class MidiSessionRepositoryImpl(private val sessionDatabase: SessionDatabase) :
    MidiSessionRepository {

    constructor(applicationContext: Context) : this(
        Room.databaseBuilder(applicationContext, SessionDatabase::class.java, "session-database")
            .build()
    )

    private var currentSession: Session? = null


    override fun startSession(): Int {
        return startNewSession().uid
    }

    override suspend fun addMessageToSession(translatedMidiMessage: MidiMessage) {
        if (currentSession == null) {
            startNewSession()
        }

        with(translatedMidiMessage) {
            sessionDatabase.sessionDao().addMidiMessage(
                SessionMidiMessage(
                    0,
                    currentSession?.uid,
                    midiCommand.ordinal,
                    midiData,
                    channel,
                    timestamp
                )
            )
        }
    }

    override suspend fun getMessagesForSession(sessionId: Int): List<SessionMidiMessage> {
        return sessionDatabase.sessionDao().getAllMidiMessagesForSession(sessionId)
    }

    override fun closeSession() {
        TODO("Not yet implemented")
    }

    private fun startNewSession(): Session {
        return Session(start = now()).let {
            currentSession = it
            it
        }
    }

}