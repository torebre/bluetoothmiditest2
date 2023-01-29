package com.kjipo.bluetoothmidi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kjipo.bluetoothmidi.session.MidiSessionRepository
import com.kjipo.bluetoothmidi.ui.sessionlist.ExportFileHelpers
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
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

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

        Timber.tag("Session").i("Export sessions to $exportedSessionsFile")

        viewModelScope.launch {
           val sessions = midiSessionRepository.getSessions(sessionsToExport)

            withContext(Dispatchers.IO) {
                val sessionMidiMessageMap = sessionsToExport.map { sessionId ->
                    Pair(sessionId, midiSessionRepository.getMessagesForSession(sessionId))
                }.toMap()

                ExportFileHelpers.createZipFile(sessions, exportedSessionsFile, sessionMidiMessageMap)
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