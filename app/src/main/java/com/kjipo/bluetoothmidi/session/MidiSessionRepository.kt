package com.kjipo.bluetoothmidi.session

import com.kjipo.bluetoothmidi.midi.MidiMessage

interface MidiSessionRepository {

    suspend fun startSession(): Long

    suspend fun addMessageToSession(translatedMidiMessage: MidiMessage)

    suspend fun getMessagesForSession(sessionId: Long): List<SessionMidiMessage>

    suspend fun closeSession()

    suspend fun getStoredSessions(): List<Session>

    suspend fun getCurrentSession(): Session?

    suspend fun getSessions(sessionIds: Collection<Long>): List<Session>

    suspend fun getSessionsAndMessages(sessionIds: Collection<Long>): List<SessionWithMessages>

}