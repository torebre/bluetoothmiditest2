package com.kjipo.bluetoothmidi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kjipo.bluetoothmidi.session.MidiSessionRepository
import com.kjipo.bluetoothmidi.ui.sessionlist.MidiSessionData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

class MidiSessionListViewModel(
    private val midiSessionRepository: MidiSessionRepository
) : ViewModel() {

    private val viewModelState = MutableStateFlow(MidiSessionUiState(emptyList()))

    val uiState =
        viewModelState.stateIn(viewModelScope, SharingStarted.Eagerly, viewModelState.value)

    init {
        viewModelScope.launch {
            val storedSessions = midiSessionRepository.getStoredSessions().map { midiSession ->
                MidiSessionData(
                    midiSession.uid,
                    LocalDateTime.ofInstant(midiSession.start, ZoneId.systemDefault()),
                    LocalDateTime.ofInstant(midiSession.sessionEnd, ZoneId.systemDefault())
                )
            }
            viewModelState.update { it.copy(storedSessions = storedSessions) }
        }
    }

    fun exportData(sessionsToExport: Collection<Long>, exportedSessionsFile: File, shareCallback: () -> Unit) {
//        getFileUrl =
//            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
//                activityResult.data?.data?.let { saveData(it) }
//            }

//        getFileUrl.launch(createDocumentIntent)

//        val openDocumentTreeIntent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
//            addCategory(Intent.CATEGORY_OPENABLE)
//            type = "application/text"
//            putExtra(Intent.EXTRA_TITLE, getDefaultTitle())
//        }

//        context.startActivity(Intent.createChooser(openDocumentTreeIntent, "Export sessions"))

        Timber.tag("Session").i("Export sessions to $exportedSessionsFile")

        viewModelScope.launch {
           val sessions = midiSessionRepository.getSessions(sessionsToExport)

            withContext(Dispatchers.IO) {
                FileOutputStream(exportedSessionsFile).use { fileOutputStream ->
                    ZipOutputStream(fileOutputStream).use {zipOutputStream ->
                        sessions.map { session ->
                            // TODO
//                        midiSessionRepository.getMessagesForSession(session.uid)

                            val zipEntry = ZipEntry(session.uid.toString())

                            zipOutputStream.putNextEntry(zipEntry)

                            val byteArrayInputStream = session.uid.toString().byteInputStream()

                            // TODO Write MIDI-messages to file
                            while(byteArrayInputStream.available() > 0) {
                                zipOutputStream.write(byteArrayInputStream.read())
                            }

                        }

                    }

                }

                shareCallback()

            }








        }

    }

    companion object {

        private fun getDefaultTitle() =
            "midi_output_${LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)}.json"

        fun provideFactory(midiSessionRepository: MidiSessionRepository): ViewModelProvider.Factory =
            object: ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MidiSessionListViewModel(midiSessionRepository) as T
            }
        }

    }

}


data class MidiSessionUiState(
    val storedSessions: List<MidiSessionData>
)