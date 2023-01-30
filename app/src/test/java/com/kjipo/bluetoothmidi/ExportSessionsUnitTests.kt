package com.kjipo.bluetoothmidi

import com.kjipo.bluetoothmidi.session.Session
import com.kjipo.bluetoothmidi.session.SessionWithMessages
import com.kjipo.bluetoothmidi.ui.sessionlist.ExportFileHelpers
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExportSessionsUnitTests {

    @Test
    fun sessionsCanBeExportedAndReadAgain() {
        val session = Session(10,
            Instant.now().truncatedTo(ChronoUnit.SECONDS),
            Instant.now().truncatedTo(ChronoUnit.SECONDS))

        val sessionWithMessages = SessionWithMessages(session, generateMidiMessages(10))

        val session2 = Session(20,
            Instant.now().truncatedTo(ChronoUnit.SECONDS),
            Instant.now().truncatedTo(ChronoUnit.SECONDS))

        val sessionWithMessages2 = SessionWithMessages(session2, generateMidiMessages(10))
        val sessionsToExport = listOf(sessionWithMessages, sessionWithMessages2)

        val tempFile = File.createTempFile("exported_sessions", "zip")
        ExportFileHelpers.createZipFile(sessionsToExport, tempFile)

        val importedSessionsWithMessages = ExportFileHelpers.readZipFile(tempFile)

        assertEquals(sessionsToExport, importedSessionsWithMessages)
    }

}