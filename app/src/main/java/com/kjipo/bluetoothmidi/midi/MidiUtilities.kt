package com.kjipo.bluetoothmidi.midi

import timber.log.Timber


    fun pitchToNoteAndOctave(pitch: Int): Pair<String, Int> {
        val remainder = pitch.rem(12)
        val noteType = when (remainder) {
            9 -> "A"
            10 -> "A#"
            11 -> "H"
            0 -> "C"
            1 -> "C#"
            2 -> "D"
            3 -> "D#"
            4 -> "E"
            5 -> "F"
            6 -> "F#"
            7 -> "G"
            8 -> "G#"
            else -> throw IllegalArgumentException("Unhandled pitch: $pitch")
        }

        return Pair(noteType, pitch.minus(remainder).div(12))
    }


@ExperimentalUnsignedTypes
fun translateMidiMessage(data: UByteArray, inputOffset: Int, timestamp: Long): MidiMessage {
    var offset = inputOffset
    val statusByte = data[offset++]
    val status = statusByte.toInt()

    Timber.i("Status byte: $statusByte. Status: $status")

    val statusAsString = getName(status)
    val numData = getBytesPerMessage(statusByte.toInt()) - 1
    val channel = if (status in 0x80..0xef) {
        status and 0x0F
    } else {
        null
    }
    val midiData = (0 until numData).map { data[offset + it] }.joinToString()

    return MidiMessage(statusAsString, midiData, channel, timestamp)
}

private fun getName(status: Int): MidiCommand {
    return when {
        status >= 0xF0 -> {
            val index = status and 0x0F
            MidiCommand.values()[index]
        }
        status >= 0x80 -> {
            val index = status shr 4 and 0x07
            MidiCommand.values()[index + NUMBER_OF_SYSTEM_COMMANDS]
        }
        else -> {
            MidiCommand.Data
        }
    }
}
