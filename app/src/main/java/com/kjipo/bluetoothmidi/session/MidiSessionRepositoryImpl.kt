package com.kjipo.bluetoothmidi.session

import android.content.Context
import androidx.room.Room
import java.time.Instant.now

class MidiSessionRepositoryImpl(applicationContext: Context) : MidiSessionRepository {

    private var currentSession: Session? = null


    private val sessionDatabase =
        Room.databaseBuilder(applicationContext, SessionDatabase::class.java, "session-database")

    override fun addMessageToSession() {
        if(currentSession == null) {
            currentSession = Session(start = now())
        }



    }

    override fun closeSession() {
        TODO("Not yet implemented")
    }

}