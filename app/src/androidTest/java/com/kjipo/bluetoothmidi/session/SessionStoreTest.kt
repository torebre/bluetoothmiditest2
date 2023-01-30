package com.kjipo.bluetoothmidi.session

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kjipo.bluetoothmidi.midi.MidiCommand
import com.kjipo.bluetoothmidi.midi.MidiMessage
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant.now


@RunWith(AndroidJUnit4::class)
class SessionStoreTest {
    private lateinit var sessionDao: SessionDao
    private lateinit var database: SessionDatabase
    private lateinit var context: Context



    @Before
    fun createDatabase() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, SessionDatabase::class.java).build()
        sessionDao = database.sessionDao()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun storeSessionTest() = runBlocking {
        val start = now()
        val session = Session(start = start)
        sessionDao.insertSession(session)

        val sessions = sessionDao.getAllSessions()

        assertThat(sessions.size, equalTo(1))
        assertThat(sessions[0].start, equalTo(start))
    }

    @Test
    fun midiMessageAdded() = runBlocking {
        val sessionMidiMessage = SessionMidiMessage(
            0, 1,
            MidiCommand.NoteOn.ordinal,
            "",
            0,
            now().toEpochMilli()
        )
        sessionDao.addMidiMessage(sessionMidiMessage)
        val midiMessages = sessionDao.getAllMidiMessagesForSession(1)

        assertThat(midiMessages.size, equalTo(1))
    }

    @Test
    fun midiMessagesCanBeAddedToSession() = runBlocking {
        val midiSession = MidiSessionRepositoryImpl(database)
        val sessionId = midiSession.startSession()
        val midiMessage = MidiMessage(
            MidiCommand.NoteOn,
            "",
            0,
            now().toEpochMilli()
        )
        midiSession.addMessageToSession(midiMessage)
        val midiMessages = midiSession.getMessagesForSession(sessionId)

        assertThat(midiMessages.size, equalTo(1))
    }

    @Test
    fun getSessionsWithMessagesTest() = runBlocking {
        val midiSession = MidiSessionRepositoryImpl(database)
        val sessionId = midiSession.startSession()
        val midiMessages = generateMidiMessages(sessionId)

        midiMessages.forEach {
            sessionDao.addMidiMessage(it)
        }
        midiSession.closeSession()

        val sessions = sessionDao.getSessionsWithMessages(listOf(sessionId))

        assertThat(sessions.size, equalTo(1))
        assertThat(sessions[0].sessionMidiMessages.size, equalTo(midiMessages.size))
    }


}