package com.kjipo.bluetoothmidi.ui.midirecord

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.kjipo.bluetoothmidi.BluetoothDeviceData
import timber.log.Timber


@Composable
fun MidiDeviceList(
    toggleScan: () -> Unit,
    isScanning: Boolean,
    connect: (String) -> Unit, foundDevices: List<BluetoothDeviceData>
) {
    val selectedDevice = remember {
        mutableStateOf("address")
    }

    Column {
        foundDevices.forEach { bluetoothDeviceData ->
            MidiDeviceEntry(MidiDeviceEntryInput(bluetoothDeviceData, selectedDevice))
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
            Column {
                ConnectButton(selectedDevice.value, onClick = {
                    selectedDevice.value.apply(connect)
                })
            }
        }
    }
}


class SampleBluetoothDeviceDataProvider: PreviewParameterProvider<MidiDeviceEntryInput> {
   override val values = sequenceOf(MidiDeviceEntryInput(BluetoothDeviceData("Test device", "12345", 1), mutableStateOf("Test")))
}

data class MidiDeviceEntryInput(val midiDevice: BluetoothDeviceData,
                           val selectedDevice: MutableState<String>)

@Preview(showBackground = true)
@Composable
fun MidiDeviceEntry(@PreviewParameter(SampleBluetoothDeviceDataProvider::class) midiDeviceEntryInput: MidiDeviceEntryInput) {
    LazyRow(Modifier.selectable(midiDeviceEntryInput.midiDevice.address == midiDeviceEntryInput.selectedDevice.value) {
        midiDeviceEntryInput.selectedDevice.value = midiDeviceEntryInput.midiDevice.address
    }) {
        item {
            Text(midiDeviceEntryInput.midiDevice.name, textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge)
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