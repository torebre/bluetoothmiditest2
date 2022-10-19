package com.kjipo.bluetoothmidi.ui.midirecord

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.kjipo.bluetoothmidi.BluetoothDeviceData
import timber.log.Timber


@Composable
fun MidiDeviceList(
    toggleScan: () -> Unit, isScanning: Boolean,
    connect: (String) -> Unit, foundDevices: List<BluetoothDeviceData>
) {
    val selectedDevice = remember {
        mutableStateOf("address")
    }

    Column {
        Row {
            foundDevices.forEach { bluetoothDeviceData ->
                MidiDeviceEntry(bluetoothDeviceData, selectedDevice)
            }
        }
        Row {
            Column {
                ScanButton(
                    onClick = {
                        Timber.tag("Bluetooth").i("Scan button pressed")
                        toggleScan()
                    },
                    isScanning
                )
            }
        }
        Column {
            ConnectButton(selectedDevice.value, onClick = {
                selectedDevice.value.apply(connect)
            })
        }
    }
}


@Composable
fun MidiDeviceEntry(midiDevice: BluetoothDeviceData, selectedDevice: MutableState<String>) {
    LazyRow(Modifier.selectable(midiDevice.bluetoothDevice.address == selectedDevice.value) {
        selectedDevice.value = midiDevice.bluetoothDevice.address
    }) {
        item {
            Text(midiDevice.toString())
        }
    }
}


@Composable
fun ScanButton(onClick: () -> Unit, isScanning: Boolean) {
    Button(onClick) {
        Text(if (isScanning) "Stop" else "Scan")
    }
}

@Composable
fun ConnectButton(selectedDevice: String, onClick: () -> Unit) {
    Button(enabled = selectedDevice != "address", onClick = onClick) {
        Text("Connect")
    }
}