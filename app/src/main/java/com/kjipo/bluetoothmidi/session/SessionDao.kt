package com.kjipo.bluetoothmidi.session

import androidx.room.*


@Dao
interface SessionDao {

    @Insert
    fun insertSession(session: Session)

    @Update
    fun updateSession(session: Session)

    @Delete
    fun deleteSession(session: Session)

    @Query("SELECT * FROM session")
    fun getAllSessions(): List<Session>

}