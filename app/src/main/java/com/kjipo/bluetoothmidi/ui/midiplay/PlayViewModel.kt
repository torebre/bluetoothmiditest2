package com.kjipo.bluetoothmidi.midi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class PlayViewModel(private val midiHandler: MidiHandler): ViewModel() {

    private val inputByteBuffer = ByteArray(3)

    private val viewModelState = MutableStateFlow(PlayViewModelState(false))

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
        midiCommand(MidiConstants.STATUS_NOTE_ON.value, 60, 64)

        withContext(Dispatchers.IO) {
            Thread.sleep(5000)
        }

        midiCommand(MidiConstants.STATUS_NOTE_OFF.value, 60, 64)
    }

    private fun midiCommand(status: UByte, data1: Int, data2: Int) {
        inputByteBuffer[0] = status.toByte()
        inputByteBuffer[1] = data1.toByte()
        inputByteBuffer[2] = data2.toByte()
        val now = System.nanoTime()

        midiHandler.send(inputByteBuffer, 3, now)
    }

    companion object {

        fun provideFactory(midiHandler: MidiHandler): ViewModelProvider.Factory =
            object: ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return PlayViewModel(midiHandler) as T
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