package com.kjipo.bluetoothmidi.ui.midirecord

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import com.kjipo.bluetoothmidi.BluetoothDeviceData


@Composable
fun MidiDeviceList(foundDevices: List<BluetoothDeviceData>) {

    Column {
        foundDevices.forEach { bluetoothDeviceData ->
            MidiDeviceEntry(bluetoothDeviceData)


        }

    }




}


@Composable
fun MidiDeviceEntry(midiDevice: BluetoothDeviceData) {
    Row {
        Text(midiDevice.toString())
    }


}