package com.kjipo.bluetoothmidi.session

import com.kjipo.bluetoothmidi.midi.MidiMessage

interface MidiSessionRepository {

    fun startSession()

    suspend fun addMessageToSession(translatedMidiMessage: MidiMessage)

    fun closeSession()

}