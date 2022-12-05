package com.kjipo.bluetoothmidi.session

import androidx.room.TypeConverter
import java.time.Instant


class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Instant? {
        return value?.let { Instant.ofEpochMilli(it) }
    }

    @TypeConverter
    fun dateToTimestamp(instant: Instant?): Long? {
        return instant?.let { it.toEpochMilli() }
    }
}