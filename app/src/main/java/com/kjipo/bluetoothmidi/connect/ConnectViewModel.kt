package com.kjipo.bluetoothmidi.connect

import android.bluetooth.BluetoothManager
import android.content.Context
import android.media.midi.MidiManager
import android.media.midi.MidiReceiver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kjipo.bluetoothmidi.session.MidiSessionRepository
import com.kjipo.bluetoothmidi.midi.MidiMessageHandler
import com.kjipo.bluetoothmidi.midi.MidiMessageTranslator
import com.kjipo.bluetoothmidi.midi.translateMidiMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

@OptIn(ExperimentalUnsignedTypes::class)
class ConnectViewModel(
    applicationContext: Context,
    address: String,
    private val midiSessionRepository: MidiSessionRepository
) : ViewModel() {

    private val viewModelState = MutableStateFlow(MidiDeviceConnectUiState())

    val uiState =
        viewModelState.stateIn(viewModelScope, SharingStarted.Eagerly, viewModelState.value)


    init {
        val bluetoothManager =
            applicationContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

        // TODO Should this use a lifecycle-scope instead?
        viewModelScope.launch {
            val deviceToOpen =
                bluetoothManager.adapter.bondedDevices.firstOrNull { it.address == address }


            Timber.tag("MIDI").i("Device to open: $deviceToOpen")

            if (deviceToOpen != null) {
                val midiManager =
                    applicationContext.getSystemService(Context.MIDI_SERVICE) as MidiManager

                midiManager.openBluetoothDevice(
                    deviceToOpen,
                    { device ->
                        Timber.tag("MIDI").i("Device opened: $device")

                        viewModelState.update { it.copy(connected = true) }
                        // TODO When will this be closed? Is it when the application closes or earlier?
                        addCloseable(device)

                        device.let { midiDevice ->
                            midiDevice.info.ports.forEach { portInfo ->
                                Timber.tag("MIDI")
                                    .i("Name: ${portInfo.name} Type: ${portInfo.type} Port number: ${portInfo.portNumber}")
                            }
                        }

                        val outputPort = device.openOutputPort(0)
                        if (outputPort == null) {
                            Timber.tag("MIDI").e("Could not open port")
                        } else {
                            Timber.tag("MIDI").i("Opened output port: $outputPort")

                            outputPort.connect(getMidiReceiver(getMidiMessageTranslator()))
                        }
                    },
                    // TODO Is it necessary to specify a handler?
                    null
                )
            } else {
                viewModelState.update { it.copy(deviceNotFound = true) }
            }
        }
    }


    fun closeSession() {
        midiSessionRepository.closeSession()
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
            address: String,
            midiSessionRepository: MidiSessionRepository
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ConnectViewModel(applicationContext, address, midiSessionRepository) as T
                }
            }

    }


}


data class MidiDeviceConnectUiState(
    val midiDeviceToOpen: String = "",
    val connected: Boolean = false,
    val numberOfReceivedMessages: Int = 0,
    val deviceNotFound: Boolean = false
)

