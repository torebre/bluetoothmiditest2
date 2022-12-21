package com.kjipo.bluetoothmidi.session

import android.content.Context
import androidx.room.Room
import com.kjipo.bluetoothmidi.midi.MidiMessage
import java.time.Instant.now

class MidiSessionRepositoryImpl(private val sessionDatabase: SessionDatabase) :
    MidiSessionRepository {

    private var currentSession: Session? = null

    constructor(applicationContext: Context) : this(
        Room.databaseBuilder(applicationContext, SessionDatabase::class.java, "session-database")
            .build()
    )

    override suspend fun startSession(): Int {
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

    override suspend fun closeSession() {
        currentSession?.let {
            sessionDatabase
            it.sessionEnd = now()
            sessionDatabase.sessionDao().updateSession(it)
        }
    }

    private suspend fun startNewSession(): Session {
        return Session(start = now()).let {
            currentSession = it
            sessionDatabase.sessionDao().insertSession(it)
            it
        }
    }

    override suspend fun getStoredSessions(): List<Session> {
        return sessionDatabase.sessionDao().getAllSessions().filter { it.sessionEnd != null }
    }


}