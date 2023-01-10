package com.kjipo.bluetoothmidi.session

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Session::class, SessionMidiMessage::class], version = 2)
@TypeConverters(Converters::class)
abstract class SessionDatabase : RoomDatabase() {

    abstract fun sessionDao(): SessionDao

}