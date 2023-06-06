package com.kjipo.bluetoothmidi.midi

import android.bluetooth.BluetoothManager
import android.content.Context
import android.media.midi.MidiDevice
import android.media.midi.MidiDeviceInfo.PortInfo.TYPE_INPUT
import android.media.midi.MidiInputPort
import android.media.midi.MidiManager
import android.media.midi.MidiManager.OnDeviceOpenedListener
import android.media.midi.MidiOutputPort
import android.media.midi.MidiReceiver
import timber.log.Timber

class MidiHandler(private val bluetoothManager: BluetoothManager,
                  private val midiManager: MidiManager) : OnDeviceOpenedListener {
    private var midiDeviceNullable: MidiDevice? = null
    private var inputPort: MidiInputPort? = null
    private var outputPort: MidiOutputPort? = null

    // TODO Should be volatile since it is being accessed from different threads
    private var address: String? = null


    fun openDevice(address: String): Boolean {
        if (midiDeviceNullable != null) {
            // Expecting that only one device will be opened
            throw IllegalStateException("MIDI device already opened")
        }

//        val bluetoothManager =
//            applicationContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

        val deviceToOpen =
            bluetoothManager.adapter.bondedDevices.firstOrNull { it.address == address }

        Timber.tag("MIDI").i("Device to open: $deviceToOpen")

        if (deviceToOpen != null) {
//            val midiManager =
//                applicationContext.getSystemService(Context.MIDI_SERVICE) as MidiManager

            midiManager.openBluetoothDevice(
                deviceToOpen,
                this,
                // TODO Is it necessary to specify a handler?
                null
            )
            this.address = address

            return true
        }
        return false
    }


    fun connectToOutputPort(midiReceiver: MidiReceiver): Boolean {
        return getOutputPort().let { midiOutputPort ->
            if (midiOutputPort == null) {
                false
            } else {
                midiOutputPort.connect(midiReceiver)
                true
            }
        }
    }


    fun removeFromOutputPort(midiReceiver: MidiReceiver) {
        getOutputPort()?.disconnect(midiReceiver)
    }


    private fun getInputMidiPort(): MidiInputPort? {
        return midiDeviceNullable.let { midiDevice ->
            if (midiDevice == null) {
                throw IllegalStateException("Midi device not set up")
            }
            openMidiInputPortIfNotAlreadyOpen(midiDevice)
        }

    }


    private fun getOutputPort(): MidiOutputPort? {
        if (outputPort != null) {
            return outputPort
        }

        outputPort = midiDeviceNullable.let { midiDevice ->
            if (midiDevice == null) {
                throw IllegalStateException("Midi device not set up")
            }
            midiDevice.openOutputPort(0)

        }

        if (outputPort == null) {
            Timber.tag("MIDI").w("Could not open port")
        } else {
            Timber.tag("MIDI").d("Opened output port: $outputPort")
        }

        return outputPort
    }

    private fun openMidiInputPortIfNotAlreadyOpen(midiDevice: MidiDevice): MidiInputPort? {
        if (inputPort != null) {
            return inputPort
        }

        inputPort =
            midiDevice.info.ports.firstOrNull { it.type == TYPE_INPUT }?.let { inputPortInfo ->
                midiDevice.openInputPort(inputPortInfo.portNumber).also {
                    if (it == null) {
                        Timber.tag("MIDI").w("Could not open input port")
                    } else {
                        Timber.tag("MIDI").d("Opened input port: $it")
                    }
                }
            }.also {
                if (it == null) {
                    Timber.tag("MIDI").i("Could not find available input port")
                }
            }

        return inputPort
    }


    fun sendMidiCommand(
        status: UByte, data1: Int, data2: Int,
        timeStamp: Long = 0
    ) {
        val inputByteBuffer = ByteArray(3)

        inputByteBuffer[0] = status.toByte()
        inputByteBuffer[1] = data1.toByte()
        inputByteBuffer[2] = data2.toByte()

        getInputMidiPort()?.send(inputByteBuffer, 0, 3, timeStamp)
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




    fun close() {
        inputPort?.close()
        midiDeviceNullable?.close()
    }

}