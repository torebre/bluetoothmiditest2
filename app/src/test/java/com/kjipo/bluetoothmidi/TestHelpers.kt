package com.kjipo.bluetoothmidi

import com.kjipo.bluetoothmidi.midi.MidiCommand
import com.kjipo.bluetoothmidi.session.SessionMidiMessage
import java.time.Instant


private val noteVelocity: Byte = 64

fun generateMidiMessages(sessionId: Long): List<SessionMidiMessage> {
    return (60..72).map {
        SessionMidiMessage(
            0, sessionId,
            MidiCommand.NoteOn.ordinal,
            "${it.toByte()},${noteVelocity}",
            0,
            Instant.now().toEpochMilli()
        )
    }.toList()
}
