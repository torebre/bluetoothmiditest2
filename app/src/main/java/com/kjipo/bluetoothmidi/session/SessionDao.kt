package com.kjipo.bluetoothmidi.session

import androidx.room.*


@Dao
interface SessionDao {

    @Insert
    suspend fun insertSession(session: Session): Long

    @Update
    suspend fun updateSession(session: Session)

    @Delete
    suspend fun deleteSession(session: Session)

    @Query("SELECT * FROM session")
    suspend fun getAllSessions(): List<Session>

    @Insert
    suspend fun addMidiMessage(sessionMidiMessage: SessionMidiMessage)

    @Query("SELECT id, sessionId, midiCommand, midiData, channel, timestamp FROM sessionmidimessage WHERE sessionmidimessage.sessionId = :sessionId")
    suspend fun getAllMidiMessagesForSession(sessionId: Long): List<SessionMidiMessage>

    @Query("SELECT * FROM session WHERE uid = :sessionId")
    suspend fun getSession(sessionId: Long): Session?

    @Query("SELECT * FROM session WHERE uid IN (:sessionIds)")
    suspend fun getSessions(sessionIds: Collection<Long>): List<Session>

    @Transaction
    @Query("SELECT * FROM session WHERE uid IN (:sessionIds)")
    suspend fun getSessionsWithMessages(sessionIds: Collection<Long>): List<SessionWithMessages>

    @Query("SELECT * FROM session WHERE session.stop != null ORDER BY session.stop LIMIT 1")
    fun getMostRecentSession(): Session?

}