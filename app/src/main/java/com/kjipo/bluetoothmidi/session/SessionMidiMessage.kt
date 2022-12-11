package com.kjipo.bluetoothmidi.session

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class SessionMidiMessage(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    val midiCommand: Int?,
    val midiData: String?,
    val channel: Int?,
    val timestamp: Long?
)