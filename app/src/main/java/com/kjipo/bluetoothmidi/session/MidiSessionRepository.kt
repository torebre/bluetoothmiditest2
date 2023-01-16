package com.kjipo.bluetoothmidi.session

import com.kjipo.bluetoothmidi.midi.MidiMessage

interface MidiSessionRepository {

    suspend fun startSession(): Long

    suspend fun addMessageToSession(translatedMidiMessage: MidiMessage)

    suspend fun getMessagesForSession(sessionId: Long): List<SessionMidiMessage>

    suspend fun closeSession()

    suspend fun getStoredSessions(): List<Session>

    suspend fun getCurrentSession(): Session?


}