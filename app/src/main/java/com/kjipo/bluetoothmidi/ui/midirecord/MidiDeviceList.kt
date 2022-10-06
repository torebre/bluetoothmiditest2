package com.kjipo.bluetoothmidi.ui.midirecord

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.kjipo.bluetoothmidi.BluetoothDeviceData


@Composable
fun MidiDeviceList(toggleScan: () -> Unit, isScanning: Boolean,
                   connect: (String) -> Unit, foundDevices: List<BluetoothDeviceData>) {
    val selectedDevice = remember {
        mutableStateOf("address")
    }

    Column {
        foundDevices.forEach { bluetoothDeviceData ->
            MidiDeviceEntry(bluetoothDeviceData, selectedDevice)
        }

        ScanButton(onClick = toggleScan, isScanning)
        ConnectButton(selectedDevice.value, onClick = {
            selectedDevice.value.apply(connect)
        })
    }

}


@Composable
fun MidiDeviceEntry(midiDevice: BluetoothDeviceData, selectedDevice: MutableState<String>) {
    Row(Modifier.selectable(midiDevice.bluetoothDevice.address == selectedDevice.value) {
        selectedDevice.value = midiDevice.bluetoothDevice.address
    }) {
        Text(midiDevice.toString())
    }
}


@Composable
fun ScanButton(onClick: () -> Unit, isScanning: Boolean) {
    Button(onClick) {
        Text(if(isScanning) "Stop" else "Scan")
    }
}

@Composable
fun ConnectButton(selectedDevice: String, onClick: () -> Unit) {
    Button(enabled = selectedDevice != "address", onClick = onClick) {
        Text("Connect")
    }
}