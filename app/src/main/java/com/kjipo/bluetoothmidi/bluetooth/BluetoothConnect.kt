package com.kjipo.bluetoothmidi.bluetooth

import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.companion.CompanionDeviceManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable


@Composable
fun BluetoothConnect(bluetoothPairing: BluetoothPairing) {
    val startSenderForResult = ActivityResultContracts.StartIntentSenderForResult()

    val launcher =
        rememberLauncherForActivityResult(contract = startSenderForResult) { activityResult ->
            when (activityResult.resultCode) {
                Activity.RESULT_OK -> {
                    // The user chose to pair the app with a Bluetooth device.
                    val deviceToPair: BluetoothDevice? =
                        activityResult.data?.getParcelableExtra(CompanionDeviceManager.EXTRA_DEVICE)
                    deviceToPair?.createBond()
                }

            }

        }

    Column {
        Button(onClick = {
            bluetoothPairing.startScan(launcher)
        }) {
            Text(text = "Scan")
        }

    }


}