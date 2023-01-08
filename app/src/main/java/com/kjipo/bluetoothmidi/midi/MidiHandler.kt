package com.kjipo.bluetoothmidi.midi

import android.bluetooth.BluetoothManager
import android.content.Context
import android.media.midi.MidiDevice
import android.media.midi.MidiDeviceInfo.PortInfo.TYPE_INPUT
import android.media.midi.MidiInputPort
import android.media.midi.MidiManager
import android.media.midi.MidiManager.OnDeviceOpenedListener
import android.media.midi.MidiReceiver
import timber.log.Timber

class MidiHandler(private val applicationContext: Context) : OnDeviceOpenedListener {
    private var midiDeviceNullable: MidiDevice? = null
    private var inputPort: MidiInputPort? = null


    fun openDevice(address: String): Boolean {
        if(midiDeviceNullable != null) {
            // Expecting that only one device will be opened
            throw IllegalStateException("MIDI device already opened")
        }

        val bluetoothManager =
            applicationContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

        val deviceToOpen =
            bluetoothManager.adapter.bondedDevices.firstOrNull { it.address == address }


        Timber.tag("MIDI").i("Device to open: $deviceToOpen")

        if (deviceToOpen != null) {
            val midiManager =
                applicationContext.getSystemService(Context.MIDI_SERVICE) as MidiManager

            midiManager.openBluetoothDevice(
                deviceToOpen,
                this,
                // TODO Is it necessary to specify a handler?
                null
            )
            return true
        }
        return false
    }


    fun connectToOutputPort(midiReceiver: MidiReceiver) {
        midiDeviceNullable.let {
            if(it == null) {
                throw IllegalStateException("Midi device not set up")
            }

            val outputPort = it.openOutputPort(0)
            if (outputPort == null) {
                Timber.tag("MIDI").w("Could not open port")
            } else {
                Timber.tag("MIDI").d("Opened output port: $outputPort")

                outputPort.connect(midiReceiver)
            }
        }
    }


    fun removeFromOutputPort(midiReceiver: MidiReceiver) {
        midiDeviceNullable?.let {
            val outputPort = it.openOutputPort(0)
            if (outputPort == null) {
                Timber.tag("MIDI").w("Could not open port")
            } else {
                Timber.tag("MIDI").d("Opened output port: $outputPort")

                outputPort.disconnect(midiReceiver)
            }
        }
    }

    fun send(msg: ByteArray,
                     count: Int,
                     timestamp: Long) {
        if(inputPort == null) {
            openInputPort()
        }

        inputPort.let {
            if(it == null) {
                Timber.tag("MIDI").w("MIDI input port not opened")
                return@let
            }

            it.send(msg, 0, count, timestamp)
        }
    }

    private fun openInputPort() {
        midiDeviceNullable.let { midiDevice ->
            if(midiDevice == null) {
                throw IllegalStateException("Midi device not set up")
            }

            val port = midiDevice.info.ports.firstOrNull { it.type == TYPE_INPUT }
            if(port != null) {
                inputPort = midiDevice.openInputPort(port.portNumber).also {
                    if (it == null) {
                        Timber.tag("MIDI").w("Could not open input port")
                    } else {
                        Timber.tag("MIDI").d("Opened input port: $it")
                    }
                }
            }
            else {
                Timber.tag("MIDI").i("Could not find available input port")
            }
        }
    }


    override fun onDeviceOpened(device: MidiDevice?) {
        device?.let {
            Timber.tag("MIDI").i("Device opened: $it")

//                    viewModelState.update { it.copy(connected = true) }
            // TODO When will this be closed? Is it when the application closes or earlier?
//                    addCloseable(device)

//            midiDevice.info.ports.forEach { portInfo ->
//                Timber.tag("MIDI")
//                    .i("Name: ${portInfo.name} Type: ${portInfo.type} Port number: ${portInfo.portNumber}")
//            }

            midiDeviceNullable = it

        }
    }

}