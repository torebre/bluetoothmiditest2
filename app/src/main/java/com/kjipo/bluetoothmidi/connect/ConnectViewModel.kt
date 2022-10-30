package com.kjipo.bluetoothmidi.connect

import android.bluetooth.BluetoothManager
import android.content.Context
import android.media.midi.MidiDevice
import android.media.midi.MidiManager
import android.media.midi.MidiReceiver
import android.os.Handler
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

class ConnectViewModel(applicationContext: Context, address: String) : ViewModel() {

    private val viewModelState = MutableStateFlow(MidiDeviceConnectUiState())

    private lateinit var openedMidiDevice: MidiDevice

    val uiState =
        viewModelState.stateIn(viewModelScope, SharingStarted.Eagerly, viewModelState.value)


    init {
        val bluetoothManager =
            applicationContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

        viewModelScope.launch {
            val deviceToOpen =
                bluetoothManager.adapter.bondedDevices.firstOrNull { it.address == address }

            Timber.tag("MIDI").i("Device to open: $deviceToOpen")

            if (deviceToOpen != null) {
                val midiManager =
                    applicationContext.getSystemService(Context.MIDI_SERVICE) as MidiManager

                midiManager.openBluetoothDevice(deviceToOpen,
                    { device ->
                        Timber.tag("MIDI").i("Device opened: $device")

                        viewModelState.update { it.copy(connected = true) }

                        openedMidiDevice = device

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

                            outputPort.connect(object : MidiReceiver() {
                                override fun onSend(
                                    msg: ByteArray?,
                                    offset: Int,
                                    count: Int,
                                    timestamp: Long
                                ) {

                                    // TODO Handle MIDI message

//                                msg?.let { messageBytes ->
//                                    Timber.d("Offset: $offset. Count: $count. Timestamp: $timestamp. Bytes in message: ${messageBytes.joinToString { messageBytes.toString() }}")
//                                    midiMessageTranslator.onSend(msg, offset, count, timestamp)
//                                }
//
//                                runOnUiThread {
//                                    dataView.append("Got message. Count: $count\n")
//                                }

                                    viewModelState.update {
                                        it.copy(numberOfReceivedMessages = it.numberOfReceivedMessages + 1)
                                    }
                                }
                            })
                        }
                    }, Handler { msg ->
                        Timber.i("Message: $msg")
                        true
                    }
                )

            }

        }


    }


    companion object {

        fun provideFactory(
            applicationContext: Context,
            address: String
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ConnectViewModel(applicationContext, address) as T
                }
            }

    }


}


data class MidiDeviceConnectUiState(
    val connected: Boolean = false,
    val numberOfReceivedMessages: Int = 0
)

