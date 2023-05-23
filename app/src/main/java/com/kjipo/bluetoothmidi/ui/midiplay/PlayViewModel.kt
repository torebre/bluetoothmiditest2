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
import java.util.*
import kotlin.collections.ArrayList


class PlayViewModel(private val midiHandler: MidiHandler, private val earTrainer: EarTrainer) :
    ViewModel() {

    private val viewModelState = MutableStateFlow(PlayViewModelState(PlayState.WAITING).toUiState())

    val uiState =
        viewModelState.stateIn(viewModelScope, SharingStarted.Eagerly, viewModelState.value)

    private val receivedMessages: MutableList<MidiMessage> =
        Collections.synchronizedList(ArrayList<MidiMessage>())


    init {
        viewModelScope.launch {
            midiHandler.connectToOutputPort(getMidiReceiver())
        }
    }

    fun play() {
        viewModelState.update { it.copy(playState = PlayState.PLAYING) }
        viewModelScope.launch {
            playSequence()
            viewModelState.update { it.copy(playState = PlayState.USER_INPUT) }
        }
    }


    @OptIn(ExperimentalUnsignedTypes::class)
    private suspend fun playSequence() {
        // Will send on channel 0
        withContext(Dispatchers.IO) {
            for (midiPlayCommand in earTrainer.getCurrentSequence()) {
                Timber.tag("MIDI").d("Processing command: $midiPlayCommand")

                when (midiPlayCommand) {
                    is NoteOn -> {
                        sendMidiCommand(
                            MidiConstants.STATUS_NOTE_ON.value,
                            midiPlayCommand.pitch,
                            midiPlayCommand.velocity
                        )
                    }

                    is NoteOff -> {
                        sendMidiCommand(
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
        }

    }

    private fun sendMidiCommand(
        status: UByte, data1: Int, data2: Int,
        timeStamp: Long = 0
    ) {
        val inputByteBuffer = ByteArray(3)

        inputByteBuffer[0] = status.toByte()
        inputByteBuffer[1] = data1.toByte()
        inputByteBuffer[2] = data2.toByte()

        midiHandler.send(inputByteBuffer, 3, timeStamp)
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    private fun getMidiReceiver(): MidiReceiver {
        return object : MidiReceiver() {
            override fun onSend(
                message: ByteArray?,
                offset: Int,
                count: Int,
                timestamp: Long
            ) {
                message?.let { messageBytes ->
                    Timber.tag("MIDI")
                        .d("Offset: $offset. Count: $count. Timestamp: $timestamp. Bytes in message: ${messageBytes.joinToString { messageBytes.toString() }}")

                    if (viewModelState.value.playState == PlayState.USER_INPUT) {
                        translateMidiMessage(
                            messageBytes.toUByteArray(),
                            offset,
                            timestamp
                        ).apply {
                            Timber.tag("MIDI").d("Translated MIDI message: $this")

                            if (isStopUserInputCommand(this)) {
                                viewModelState.update { it.copy(playState = PlayState.WAITING) }
                                earTrainer.userInputSequence(receivedMessages.toList())
                                receivedMessages.clear()
                            } else {
                                receivedMessages.add(this)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun isStopUserInputCommand(midiMessage: MidiMessage) =
        midiMessage.midiCommand == MidiCommand.NoteOn && midiMessage.splitMidiData()
            .let { it.isNotEmpty() && it[0] == "21" }

    fun playInput() {
        if (receivedMessages.isEmpty()) {
            return
        }

        viewModelScope.launch {
            val previousMessage = receivedMessages.first()
            var timeUntilNextMessage = 0L

            withContext(Dispatchers.IO) {
                for (receivedMessage in receivedMessages) {
                    Timber.tag("MidiPlay")
                        .i("Message: ${receivedMessage.midiCommand}. Timestamp: ${receivedMessage.timestamp}. Data: ${receivedMessage.splitMidiData()}")

//                    Thread.sleep(timeUntilNextMessage)

                    timeUntilNextMessage = receivedMessage.timestamp - previousMessage.timestamp
//                    receivedMessage.splitMidiData()

//                    when(receivedMessage.midiCommand) {
//                        is NoteOn -> {
//                            sendMidiCommand(
//                                MidiConstants.STATUS_NOTE_ON.value,
//                                receivedMessage.pitch,
//                                receivedMessage.velocity
//                            )
//                        }
//
//                        is NoteOff -> {
//                            sendMidiCommand(
//                                MidiConstants.STATUS_NOTE_OFF.value,
//                                midiPlayCommand.pitch,
//                                midiPlayCommand.velocity
//                            )
//                        }
//
//                    }

//                    receivedMessage.timestamp

                }

            }
        }
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
    WAITING
}

private data class PlayViewModelState(
    val playState: PlayState
) {

    fun toUiState(): PlayViewUiState {
        return PlayViewUiState(playState)
    }

}


data class PlayViewUiState(val playState: PlayState)