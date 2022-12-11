package com.kjipo.bluetoothmidi.session

import com.kjipo.bluetoothmidi.midi.MidiMessage

interface MidiSessionRepository {

    fun startSession(): Int

    suspend fun addMessageToSession(translatedMidiMessage: MidiMessage)

    suspend fun getMessagesForSession(sessionId: Int): List<SessionMidiMessage>

    fun closeSession()

}