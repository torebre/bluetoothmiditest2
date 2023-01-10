package com.kjipo.bluetoothmidi.session

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class SessionMidiMessage(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    @ColumnInfo(name = "sessionid") val sessionId: Long? = null,
    @ColumnInfo(name = "midicommand") val midiCommand: Int? = null,
    @ColumnInfo(name = "mididata") val midiData: String? = null,
    @ColumnInfo(name = "channel") val channel: Int? = null,
    @ColumnInfo(name = "timestamp") val timestamp: Long? = null
) {
    override fun toString(): String {
        return "SessionMidiMessage(id=$id, sessionId=$sessionId, midiCommand=$midiCommand, midiData=$midiData, channel=$channel, timestamp=$timestamp)"
    }
}