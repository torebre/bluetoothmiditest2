package com.kjipo.bluetoothmidi.midi

import android.media.midi.MidiReceiver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.*
import kotlin.collections.ArrayList


class PlayViewModel(private val midiHandler: MidiHandler, private val earTrainer: EarTrainer) :
    ViewModel() {

    private val inputByteBuffer = ByteArray(3)

    private val viewModelState = MutableStateFlow(PlayViewModelState(false))

    private val receivedMessages: MutableList<MidiMessage> = Collections.synchronizedList(ArrayList<MidiMessage>())



    init {
        viewModelScope.launch {
            midiHandler.connectToOutputPort(getMidiReceiver(getMidiMessageTranslator()))
        }
    }

    fun play() {
        viewModelState.update { it.copy(isPlaying = true) }
        viewModelScope.launch {
            playSequence()
            viewModelState.update { it.copy(isPlaying = false) }
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
                        midiCommand(
                            MidiConstants.STATUS_NOTE_ON.value,
                            midiPlayCommand.pitch,
                            midiPlayCommand.velocity
                        )
                    }
                    is NoteOff -> {
                        midiCommand(
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

    private fun midiCommand(status: UByte, data1: Int, data2: Int,
                            timeStamp: Long = 0) {
        inputByteBuffer[0] = status.toByte()
        inputByteBuffer[1] = data1.toByte()
        inputByteBuffer[2] = data2.toByte()

        midiHandler.send(inputByteBuffer, 3, timeStamp)
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

                // TODO Need to collect messages and send them to EarTrainer

            }
        }
    }

    private fun getMidiMessageTranslator(): MidiMessageTranslator {
        return MidiMessageTranslator(object : MidiMessageHandler {

            override fun send(msg: UByteArray, offset: Int, count: Int, timestamp: Long) {
                val translatedMidiMessage = translateMidiMessage(msg, offset, timestamp)
                viewModelScope.launch {
                    receivedMessages.add(translatedMidiMessage)
                }
            }

            override fun close() {
                TODO("Not yet implemented")
            }

        })
    }

    companion object {

        fun provideFactory(midiHandler: MidiHandler, earTrainer: EarTrainer): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return PlayViewModel(midiHandler, earTrainer) as T
                }
            }
    }

}


private data class PlayViewModelState(
    val isPlaying: Boolean
) {

    fun toUiState(): PlayViewUiState {
        return PlayViewUiState(isPlaying)
    }

}


data class PlayViewUiState(val isPlaying: Boolean)