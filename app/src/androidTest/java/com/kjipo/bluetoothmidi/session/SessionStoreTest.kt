package com.kjipo.bluetoothmidi.session

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
//import org.hamcrest.Matchers.hasSize
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant.now


@RunWith(AndroidJUnit4::class)
class SessionStoreTest {
    private lateinit var sessionDao: SessionDao
    private lateinit var database: SessionDatabase


    @Before
    fun createDatabase() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, SessionDatabase::class.java).build()
        sessionDao = database.sessionDao()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun storeSessionTest() {
        val start = now()
        val session = Session(start = start)
        sessionDao.insertSession(session)

        val sessions = sessionDao.getAllSessions()

        assertThat(sessions.size, equalTo(1))
        assertThat(sessions[0].start, equalTo(start))
    }


}