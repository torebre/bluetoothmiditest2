package com.kjipo.bluetoothmidi.ui.mididevicelist

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Text
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MidiDeviceListRoute(
    deviceListViewModel: DeviceListViewModel,
    connect: (String, SharedPreferences) -> Unit
) {
    val uiState = deviceListViewModel.uiState.collectAsState()

    val bluetoothPermissionState = if (Build.VERSION.SDK_INT <= 30) {
        rememberPermissionState(Manifest.permission.BLUETOOTH)
    } else {
        rememberPermissionState(Manifest.permission.BLUETOOTH_CONNECT)
    }


    MidiDeviceListRoute(
        uiState.value,
        toggleScan = {
            deviceListViewModel.toggleScan()
        },
        connect = connect,
        bluetoothPermissionState
    )
}


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MidiDeviceListRoute(
    uiState: MidiDevicesUiState,
    toggleScan: () -> Unit,
    connect: (String, SharedPreferences) -> Unit,
    bluetoothPermissionState: PermissionState
) {
    val selectedDevice = remember {
        mutableStateOf("address")
    }

    Column {
        when (bluetoothPermissionState.status) {
            is PermissionStatus.Granted -> {
                Text("Bluetooth connect granted")
            }

            is PermissionStatus.Denied -> {
                Row {
                    if (bluetoothPermissionState.status.shouldShowRationale) {
                        Text("Need Bluetooth connect permission")
                    } else {
                        Text("Bluetooth connect permission has been denied")
                    }
                }

                Row {
                    Button(onClick = {
                        bluetoothPermissionState.launchPermissionRequest()
                    }) {
                        Text("Request permission")
                    }

                }
            }
        }

        Row {
            MidiDeviceList(
                toggleScan,
                uiState.isScanning,
                connect,
                uiState.foundDevices,
                selectedDevice,
                uiState.connectedDeviceAddress
            )
        }
    }

}
