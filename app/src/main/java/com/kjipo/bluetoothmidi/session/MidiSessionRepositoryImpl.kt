package com.kjipo.bluetoothmidi.session

import com.kjipo.bluetoothmidi.midi.MidiMessage
import timber.log.Timber
import java.time.Instant.now

class MidiSessionRepositoryImpl(private val sessionDatabase: SessionDatabase) :
    MidiSessionRepository {

    private var currentSession: Session? = null

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

    override suspend fun getMessagesForSession(sessionId: Long): List<SessionMidiMessage> {
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

    override suspend fun getSessions(sessionIds: Collection<Long>): List<Session> {
        return sessionDatabase.sessionDao().getSessions(sessionIds)
    }

    override suspend fun getSessionsAndMessages(sessionIds: Collection<Long>): List<SessionWithMessages> {
        return sessionDatabase.sessionDao().getSessionsWithMessages(sessionIds)
    }

}