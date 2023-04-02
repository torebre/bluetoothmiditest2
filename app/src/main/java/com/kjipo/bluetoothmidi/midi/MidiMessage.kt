package com.kjipo.bluetoothmidi.midi

data class MidiMessage(
    val midiCommand: MidiCommand,
    val midiData: String,
    val channel: Int?,
    val timestamp: Long
) : java.io.Serializable {

    fun splitMidiData() = midiData.split(",")

}
