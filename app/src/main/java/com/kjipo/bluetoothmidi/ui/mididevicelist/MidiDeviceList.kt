package com.kjipo.bluetoothmidi.ui.mididevicelist

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.Dp
import com.kjipo.bluetoothmidi.BluetoothDeviceData
import com.kjipo.bluetoothmidi.PREFERENCES_KEY
import timber.log.Timber


@Composable
fun MidiDeviceList(
    toggleScan: () -> Unit,
    isScanning: Boolean,
    connect: (String, SharedPreferences) -> Unit,
    foundDevices: List<BluetoothDeviceData>,
    selectedDevice: MutableState<String>,
    connectedDeviceAddress: String?
) {

    Column {
        foundDevices.forEach { bluetoothDeviceData ->
            MidiDeviceEntry(
                MidiDeviceEntryInput(
                    bluetoothDeviceData,
                    selectedDevice
                )
            )
        }
        Row(Modifier.fillMaxWidth()) {
            Text(text = connectedDeviceAddress.let {
                if (it == null) {
                    "No device connected"
                } else {
                    foundDevices.filter { it.address == connectedDeviceAddress }
                        .first().name
                }
            })
        }
        Row(
            Modifier
                .fillMaxWidth()
                .padding(Dp(5.0f), Dp(5.0f)), horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ScanButton(
                onClick = {
                    Timber.tag("Bluetooth").i("Scan button pressed")
                    toggleScan()
                },
                isScanning
            )

            val sharedPreferences = LocalContext.current.applicationContext.getSharedPreferences(
                PREFERENCES_KEY, Context.MODE_PRIVATE
            )
            ConnectButton(selectedDevice.value, onClick = {
                connect(selectedDevice.value, sharedPreferences)
            })

            BluetoothConnect(bluetoothPairing = BluetoothHandler(LocalContext.current))
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
    LazyRow(
        Modifier
            .fillMaxWidth()
            .selectable(selected = midiDeviceEntryInput.midiDevice.address == midiDeviceEntryInput.selectedDevice.value,
                onClick = {
                    midiDeviceEntryInput.selectedDevice.value =
                        midiDeviceEntryInput.midiDevice.address
                })
    ) {
        item {
            Text(
                midiDeviceEntryInput.midiDevice.name,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.displaySmall
            )
        }
    }
}


@Composable
fun ScanButton(onClick: () -> Unit, isScanning: Boolean) {
    Button(onClick) {
        Text(
            if (isScanning) "Stop" else "Scan"
        )
    }
}

@Composable
fun ConnectButton(selectedDevice: String, onClick: () -> Unit) {
    Button(enabled = selectedDevice != "address", onClick = onClick) {
        Text("Connect")
    }
}

