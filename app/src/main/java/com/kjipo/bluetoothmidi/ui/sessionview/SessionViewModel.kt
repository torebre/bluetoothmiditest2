package com.kjipo.bluetoothmidi.ui.sessionview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kjipo.bluetoothmidi.session.SessionDao
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant


class SessionViewModel(private val sessionId: Long, private val sessionDao: SessionDao) :
    ViewModel() {

    private val viewModelState = MutableStateFlow(SessionUiState())

    val uiState =
        viewModelState.stateIn(viewModelScope,
            SharingStarted.Eagerly,
            viewModelState.value)

    init {
        viewModelScope.launch {
            viewModelState.update { sessionIdToUiState(sessionId) }
        }
    }

    private suspend fun sessionIdToUiState(sessionId: Long): SessionUiState {
            return sessionDao.getSession(sessionId)?.let { session ->
                 SessionUiState(session.start, session.sessionEnd)
            } ?: SessionUiState()
    }


    companion object {

        fun provideFactory(sessionId: Long, sessionDao: SessionDao): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {

                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SessionViewModel(sessionId, sessionDao) as T
                }
            }
    }

}


data class SessionUiState(val start: Instant? = null, val sessionEnd: Instant? = null)