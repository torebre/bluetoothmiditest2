package com.kjipo.bluetoothmidi.ui.midirecord

import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.Dp
import com.kjipo.bluetoothmidi.BluetoothDeviceData
import timber.log.Timber


class MidiDeviceListInput(
    val toggleScan: () -> Unit,
    val isScanning: Boolean,
    val connect: (String) -> Unit,
    val foundDevices: List<BluetoothDeviceData>
)

class MidiDeviceListDataProvider : PreviewParameterProvider<MidiDeviceListInput> {

    override val values = sequenceOf(
        MidiDeviceListInput(
            {
                // Do nothing
            },
            true,
            {
                // Do nothing
            },
            listOf(
                BluetoothDeviceData("Test device", "12345", 1),
                BluetoothDeviceData("Test device 2", "12345", 1),
                BluetoothDeviceData("Test device 3", "12345", 1),
                BluetoothDeviceData("Test device 4", "12345", 1),
                BluetoothDeviceData("Test device 5", "12345", 1)
            )
        )
    )

}

@Preview
@Composable
fun MidiDeviceList(@PreviewParameter(MidiDeviceListDataProvider::class) midiDeviceListInput: MidiDeviceListInput) {
    val selectedDevice = remember {
        mutableStateOf("address")
    }

    Column {
        midiDeviceListInput.foundDevices.forEach { bluetoothDeviceData ->
            MidiDeviceEntry(MidiDeviceEntryInput(bluetoothDeviceData, selectedDevice))
        }
        Row(Modifier.fillMaxWidth()
            .padding(Dp(5.0f), Dp(5.0f))
            , horizontalArrangement = Arrangement.SpaceEvenly) {
            ScanButton(
                onClick = {
                    Timber.tag("Bluetooth").i("Scan button pressed")
                    midiDeviceListInput.toggleScan()
                },
                midiDeviceListInput.isScanning
            )
            ConnectButton(selectedDevice.value, onClick = {
                selectedDevice.value.apply(midiDeviceListInput.connect)
            })
        }
    }
}


class SampleBluetoothDeviceDataProvider : PreviewParameterProvider<MidiDeviceEntryInput> {
    override val values = sequenceOf(
        MidiDeviceEntryInput(
            BluetoothDeviceData("Test device", "12345", 1),
            mutableStateOf("Test")
        )
    )
}

data class MidiDeviceEntryInput(
    val midiDevice: BluetoothDeviceData,
    val selectedDevice: MutableState<String>
)

@Preview(showBackground = true)
@Composable
fun MidiDeviceEntry(@PreviewParameter(SampleBluetoothDeviceDataProvider::class) midiDeviceEntryInput: MidiDeviceEntryInput) {
    LazyRow(Modifier.fillMaxWidth()
        .selectable(midiDeviceEntryInput.midiDevice.address == midiDeviceEntryInput.selectedDevice.value) {
        midiDeviceEntryInput.selectedDevice.value = midiDeviceEntryInput.midiDevice.address
    }) {
        item {
            Text(
                midiDeviceEntryInput.midiDevice.name, textAlign = TextAlign.Center,
                style = MaterialTheme.typography.displaySmall
            )
        }
    }
}


@Composable
fun ScanButton(onClick: () -> Unit, isScanning: Boolean) {
    Button(onClick) {
        Text(
            if (isScanning) "Stop" else "Scan")
    }
}

@Composable
fun ConnectButton(selectedDevice: String, onClick: () -> Unit) {
    Button(enabled = selectedDevice != "address", onClick = onClick) {
        Text("Connect")
    }
}