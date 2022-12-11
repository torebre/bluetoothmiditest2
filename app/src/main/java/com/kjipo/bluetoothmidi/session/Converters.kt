package com.kjipo.bluetoothmidi.session

import androidx.room.TypeConverter
import com.kjipo.bluetoothmidi.midi.MidiCommand
import java.time.Instant


class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Instant? {
        return value?.let { Instant.ofEpochMilli(it) }
    }

    @TypeConverter
    fun dateToTimestamp(instant: Instant?): Long? {
        return instant?.toEpochMilli()
    }

    @TypeConverter
    fun fromMidiCommand(value: MidiCommand?): Int? {
        return value?.ordinal
    }

    @TypeConverter
    fun midiCommandToInt(value: Int?): MidiCommand? {
        return value?.let { MidiCommand.values()[it] }
    }

}