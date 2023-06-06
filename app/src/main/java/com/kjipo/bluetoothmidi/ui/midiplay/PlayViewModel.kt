package com.kjipo.bluetoothmidi.ui.midiplay

import android.media.midi.MidiReceiver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kjipo.bluetoothmidi.midi.EarTrainer
import com.kjipo.bluetoothmidi.midi.MidiCommand
import com.kjipo.bluetoothmidi.midi.MidiConstants
import com.kjipo.bluetoothmidi.midi.MidiHandler
import com.kjipo.bluetoothmidi.midi.MidiMessage
import com.kjipo.bluetoothmidi.midi.MidiPlayCommand
import com.kjipo.bluetoothmidi.midi.NoteOff
import com.kjipo.bluetoothmidi.midi.NoteOn
import com.kjipo.bluetoothmidi.midi.Sleep
import com.kjipo.bluetoothmidi.midi.translateMidiMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.IOException
import java.util.*


class PlayViewModel(
    private val midiHandler: MidiHandler,
    private val earTrainer: EarTrainer
) :
    ViewModel() {

    private val viewModelState =
        MutableStateFlow(PlayViewModelState(PlayState.USER_INPUT).toUiState())

    val uiState =
        viewModelState.stateIn(viewModelScope, SharingStarted.Eagerly, viewModelState.value)

    private val receivedMessages: MutableList<MidiMessage> =
        Collections.synchronizedList(ArrayList())


    @OptIn(ExperimentalUnsignedTypes::class)
    private val midiReceiver = object : MidiReceiver() {
        override fun onSend(
            message: ByteArray?,
            offset: Int,
            count: Int,
            timestamp: Long
        ) {
            message?.let { messageBytes ->
                Timber.tag("MIDI")
                    .d("Offset: $offset. Count: $count. Timestamp: $timestamp. Bytes in message: ${messageBytes.joinToString { it.toString() }}")

                if (viewModelState.value.playState == PlayState.USER_INPUT) {
                    translateMidiMessage(
                        messageBytes.toUByteArray(),
                        offset,
                        timestamp
                    ).apply {
                        Timber.tag("MIDI").d("Translated MIDI message: $this")

                        if (isStopUserInputCommand(this)) {
                            earTrainer.userInputSequence(receivedMessages.toList())
                            receivedMessages.clear()
                            viewModelState.update { it.copy(numberOfReceivedMessages = 0) }
                        } else {
                            receivedMessages.add(this)
                            viewModelState.update { it.copy(numberOfReceivedMessages = it.numberOfReceivedMessages + 1) }
                        }
                    }
                }
            }
        }
    }


    init {
        viewModelScope.launch {
            if (!midiHandler.connectToOutputPort(midiReceiver)) {
                viewModelState.update { it.copy(playState = PlayState.ERROR) }
            }
        }
    }

    fun play() {
        viewModelState.update { it.copy(playState = PlayState.PLAYING) }
        viewModelScope.launch {
            playSequence()
            viewModelState.update { it.copy(playState = PlayState.USER_INPUT) }
        }
    }


    private suspend fun playSequence() {
        // Will send on channel 0
        withContext(Dispatchers.IO) {
            for (midiPlayCommand in earTrainer.getCurrentSequence()) {
                try {
                    processMidiPlayCommand(midiPlayCommand)
                } catch (exception: IOException) {
                    Timber.tag("MIDI").e(exception)
                }
            }
        }

    }

    @OptIn(ExperimentalUnsignedTypes::class)
    private fun processMidiPlayCommand(midiPlayCommand: MidiPlayCommand) {
        Timber.tag("MIDI").d("Processing command: $midiPlayCommand")

        when (midiPlayCommand) {
            is NoteOn -> {
                midiHandler.sendMidiCommand(
                    MidiConstants.STATUS_NOTE_ON.value,
                    midiPlayCommand.pitch,
                    midiPlayCommand.velocity
                )
            }

            is NoteOff -> {
                midiHandler.sendMidiCommand(
                    MidiConstants.STATUS_NOTE_OFF.value,
                    midiPlayCommand.pitch,
                    midiPlayCommand.velocity
                )
            }

            is Sleep -> {
                Thread.sleep(midiPlayCommand.sleepInMilliseconds)
            }
        }
    }


    private fun isStopUserInputCommand(midiMessage: MidiMessage) =
        midiMessage.midiCommand == MidiCommand.NoteOn && midiMessage.splitMidiData()
            .let { it.isNotEmpty() && it[0] == "21" }

    @OptIn(ExperimentalUnsignedTypes::class)
    fun playInput() {
        if (receivedMessages.isEmpty()) {
            return
        }

        viewModelState.update { it.copy(playState = PlayState.PLAYING_USER_INPUT) }

        viewModelScope.launch {
            var previousMessage = receivedMessages.first()
            var timeUntilNextMessage: Long

            withContext(Dispatchers.IO) {
                for (receivedMessage in receivedMessages) {
                    Timber.tag("MidiPlay")
                        .i("Message: ${receivedMessage.midiCommand}. Timestamp: ${receivedMessage.timestamp}. Data: ${receivedMessage.splitMidiData()}")

                    // The timestamp in the messages is based on System.nanoTime()
                    // so need to convert to milliseconds before doing sleep
                    timeUntilNextMessage =
                        (receivedMessage.timestamp - previousMessage.timestamp) / 1_000_000

                    Timber.tag("MidiPlay").i("Sleep time: $timeUntilNextMessage")

                    if (timeUntilNextMessage > 0) {
                        Thread.sleep(timeUntilNextMessage)
                    }

                    when (receivedMessage.midiCommand) {
                        MidiCommand.NoteOn -> {
                            extractPitchAndVelocity(receivedMessage).let { pitchAndVelocity ->
                                midiHandler.sendMidiCommand(
                                    MidiConstants.STATUS_NOTE_ON.value,
                                    pitchAndVelocity.pitch,
                                    pitchAndVelocity.velocity
                                )
                            }
                        }

                        MidiCommand.NoteOff -> {
                            extractPitchAndVelocity(receivedMessage).let { pitchAndVelocity ->
                                midiHandler.sendMidiCommand(
                                    MidiConstants.STATUS_NOTE_OFF.value,
                                    pitchAndVelocity.pitch,
                                    pitchAndVelocity.velocity
                                )
                            }
                        }

                        else -> {
                            Timber.tag("MIDI")
                                .w("Unexpected MIDI command: ${receivedMessage.midiCommand.name}")
                        }
                    }
                    previousMessage = receivedMessage
                }
                viewModelState.update { it.copy(playState = PlayState.USER_INPUT) }
            }
        }
    }


    private fun extractPitchAndVelocity(midiMessage: MidiMessage): PitchAndVelocity {
        val midiData = midiMessage.splitMidiData()

        return PitchAndVelocity(midiData[0].toInt(), midiData[1].toInt())
    }

    fun clearReceivedMessages() {
        receivedMessages.clear()
    }

    override fun onCleared() {
        super.onCleared()
        midiHandler.removeFromOutputPort(midiReceiver)
    }


    companion object {

        fun provideFactory(
            midiHandler: MidiHandler,
            earTrainer: EarTrainer
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return PlayViewModel(midiHandler, earTrainer) as T
                }
            }
    }

}


enum class PlayState {
    PLAYING,
    USER_INPUT,
    PLAYING_USER_INPUT,
    ERROR
}

private data class PlayViewModelState(
    val playState: PlayState,
    val numberOfReceivedMessages: Int = 0
) {

    fun toUiState(): PlayViewUiState {
        return PlayViewUiState(playState, numberOfReceivedMessages)
    }

}


data class PlayViewUiState(val playState: PlayState, val numberOfReceivedMessages: Int)

private data class PitchAndVelocity(val pitch: Int, val velocity: Int)
