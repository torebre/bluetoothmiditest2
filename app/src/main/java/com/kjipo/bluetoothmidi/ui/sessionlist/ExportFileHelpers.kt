package com.kjipo.bluetoothmidi.ui.sessionlist

import com.kjipo.bluetoothmidi.session.Session
import com.kjipo.bluetoothmidi.session.SessionMidiMessage
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream


object ExportFileHelpers {



     fun createZipFile(sessions: Collection<Session>, exportedSessionsFile: File, midiMessagesFetcher: Map<Long, List<SessionMidiMessage>>) {
        FileOutputStream(exportedSessionsFile).use { fileOutputStream ->
            ZipOutputStream(fileOutputStream).use { zipOutputStream ->

                sessions.map { session ->
                    val zipEntry = ZipEntry(session.uid.toString())
                    zipOutputStream.putNextEntry(zipEntry)



                    val byteArrayInputStream = session.uid.toString().byteInputStream()

                    // TODO Write MIDI-messages to file
                    while(byteArrayInputStream.available() > 0) {
                        zipOutputStream.write(byteArrayInputStream.read())
                    }


                    midiMessagesFetcher[session.uid]?.let {
                        // TODO




                    }



                }
            }
        }
     }



}