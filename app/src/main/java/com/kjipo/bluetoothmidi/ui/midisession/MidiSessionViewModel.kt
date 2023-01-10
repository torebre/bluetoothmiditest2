package com.kjipo.bluetoothmidi.connect

import android.content.Context
import android.media.midi.MidiReceiver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kjipo.bluetoothmidi.midi.MidiHandler
import com.kjipo.bluetoothmidi.midi.MidiMessageHandler
import com.kjipo.bluetoothmidi.midi.MidiMessageTranslator
import com.kjipo.bluetoothmidi.midi.translateMidiMessage
import com.kjipo.bluetoothmidi.session.MidiSessionRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.Instant

@OptIn(ExperimentalUnsignedTypes::class)
class MidiSessionViewModel(
    private val midiHandler: MidiHandler,
    private val midiSessionRepository: MidiSessionRepository,
    private val navigateToHome: () -> Unit
) : ViewModel() {

    private val viewModelState = MutableStateFlow(MidiSessionUiState())

    val uiState =
        viewModelState.stateIn(viewModelScope, SharingStarted.Eagerly, viewModelState.value)


    init {
        // TODO Should this use a lifecycle-scope instead?
        viewModelScope.launch {
            midiHandler.connectToOutputPort(getMidiReceiver(getMidiMessageTranslator()))
        }

        // TODO Start session somewhere else
        startSession()
    }

    private fun startSession() {
        viewModelScope.launch {
            midiSessionRepository.startSession()
            updateTimeTracker()
        }
    }

    private suspend fun updateTimeTracker() {
            midiSessionRepository.getCurrentSession()?.let { session ->
                if(session.sessionEnd != null) {
                    // Session is closed so time tracker should not be updated
                   return
                }
                viewModelState.update {
                    it.copy(
                        sessionDurationInSeconds = (Instant.now().toEpochMilli() - session.start.toEpochMilli()) / 1000
                    )
                }
            }
            delay(1000)
            updateTimeTracker()
    }


    fun closeSession() {
        // TODO Is this the correct scope to use?
        viewModelState.update { it.copy(closingSession = true) }
        viewModelScope.launch {
            midiSessionRepository.closeSession()
            navigateToHome()
        }
    }

    private fun getMidiReceiver(midiMessageTranslator: MidiMessageTranslator): MidiReceiver {
        return object : MidiReceiver() {
            override fun onSend(
                msg: ByteArray?,
                offset: Int,
                count: Int,
                timestamp: Long
            ) {

                // TODO Handle MIDI message

                msg?.let { messageBytes ->
                    Timber.d("Offset: $offset. Count: $count. Timestamp: $timestamp. Bytes in message: ${messageBytes.joinToString { messageBytes.toString() }}")
                    midiMessageTranslator.onSend(msg, offset, count, timestamp)
                }

                viewModelState.update {
                    it.copy(numberOfReceivedMessages = it.numberOfReceivedMessages + 1)
                }
            }
        }

    }

    private fun getMidiMessageTranslator(): MidiMessageTranslator {
        return MidiMessageTranslator(object : MidiMessageHandler {
            override fun send(msg: UByteArray, offset: Int, count: Int, timestamp: Long) {
                val translatedMidiMessage = translateMidiMessage(msg, offset, timestamp)
                viewModelScope.launch {
                    midiSessionRepository.addMessageToSession(translatedMidiMessage)
                }
            }

            override fun close() {
                TODO("Not yet implemented")
            }

        })
    }

    companion object {

        fun provideFactory(
            applicationContext: Context,
            midiHandler: MidiHandler,
            midiSessionRepository: MidiSessionRepository,
            navigateToHome: () -> Unit
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return MidiSessionViewModel(
                        midiHandler,
                        midiSessionRepository,
                        navigateToHome
                    ) as T
                }
            }

    }

}


data class MidiSessionUiState(
    val midiDeviceToOpen: String = "",
    val connected: Boolean = false,
    val numberOfReceivedMessages: Int = 0,
    val deviceNotFound: Boolean = false,
    val sessionDurationInSeconds: Long = 0,
    val closingSession: Boolean = false
)
