package com.kjipo.bluetoothmidi.session

import androidx.room.*


@Dao
interface SessionDao {

    @Insert
    suspend fun insertSession(session: Session)

    @Update
    suspend fun updateSession(session: Session)

    @Delete
    suspend fun deleteSession(session: Session)

    @Query("SELECT * FROM session")
    suspend fun getAllSessions(): List<Session>

    @Insert
    suspend fun addMidiMessage(sessionMidiMessage: SessionMidiMessage)

}