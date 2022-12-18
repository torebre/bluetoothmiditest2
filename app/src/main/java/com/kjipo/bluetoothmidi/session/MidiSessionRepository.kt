package com.kjipo.bluetoothmidi.session

import com.kjipo.bluetoothmidi.midi.MidiMessage

interface MidiSessionRepository {

    suspend fun startSession(): Int

    suspend fun addMessageToSession(translatedMidiMessage: MidiMessage)

    suspend fun getMessagesForSession(sessionId: Int): List<SessionMidiMessage>

    suspend fun closeSession()

}