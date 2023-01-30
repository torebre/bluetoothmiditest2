package com.kjipo.bluetoothmidi.ui.sessionlist

import com.kjipo.bluetoothmidi.session.SessionWithMessages
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream


object ExportFileHelpers {


    @OptIn(ExperimentalSerializationApi::class)
    fun createZipFile(sessions: Collection<SessionWithMessages>, exportedSessionsFile: File) {
        FileOutputStream(exportedSessionsFile).use { fileOutputStream ->
            ZipOutputStream(fileOutputStream).use { zipOutputStream ->
                sessions.map { session ->
                    val zipEntry = ZipEntry(session.session.uid.toString())
                    zipOutputStream.putNextEntry(zipEntry)
                    Json.encodeToStream(session, zipOutputStream)
                    zipOutputStream.closeEntry()
                }
            }
        }
    }

    fun readZipFile(exportedSessionsFile: File): MutableList<SessionWithMessages> {
        val sessionsWithMessages = mutableListOf<SessionWithMessages>()
        FileInputStream(exportedSessionsFile).use { fileInputStream ->
            ZipInputStream(fileInputStream).use { zipInputStream ->
                var zipEntry = zipInputStream.nextEntry

                while (zipEntry != null) {
                    val byteArrayOutputStream = ByteArrayOutputStream()
                    var readByte = zipInputStream.read()
                    while(readByte != -1) {
                        byteArrayOutputStream.write(readByte)
                        readByte = zipInputStream.read()
                    }
                    val readData = String(byteArrayOutputStream.toByteArray())

                    sessionsWithMessages.add(Json.decodeFromString(readData))
                    zipInputStream.closeEntry()
                    zipEntry = zipInputStream.nextEntry
                }
            }
        }
        return sessionsWithMessages
    }


}