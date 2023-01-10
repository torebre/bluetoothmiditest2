package com.kjipo.bluetoothmidi.session

import android.content.Context
import androidx.room.Room
import com.kjipo.bluetoothmidi.midi.MidiMessage
import timber.log.Timber
import java.time.Instant
import java.time.Instant.now

class MidiSessionRepositoryImpl(private val sessionDatabase: SessionDatabase) :
    MidiSessionRepository {

    private var currentSession: Session? = null

    constructor(applicationContext: Context) : this(
        Room.databaseBuilder(applicationContext, SessionDatabase::class.java, "session-database")
                // TODO Only here while developing
            .fallbackToDestructiveMigration()
            .build()
    )

    override suspend fun startSession(): Long {
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
        Timber.tag("MidiSession").i("Closing session: ${currentSession?.uid}")

        currentSession?.let {
            it.sessionEnd = now()

            Timber.tag("MidiSession").i("Setting session end to ${it.sessionEnd}")

            sessionDatabase.sessionDao().updateSession(it)
        }
    }

    private suspend fun startNewSession(): Session {
        return Session(start = now()).let {
            currentSession = it
            it.uid = sessionDatabase.sessionDao().insertSession(it)
            it
        }
    }

    override suspend fun getStoredSessions(): List<Session> {
        return sessionDatabase.sessionDao().getAllSessions().filter { it.sessionEnd != null }
    }

    override suspend fun getCurrentSession(): Session? {
        return currentSession
    }


}