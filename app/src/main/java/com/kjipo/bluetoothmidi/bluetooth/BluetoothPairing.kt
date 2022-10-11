package com.kjipo.bluetoothmidi.bluetooth

import android.companion.AssociationRequest
import android.companion.BluetoothDeviceFilter
import android.companion.CompanionDeviceManager
import android.content.Context
import android.content.IntentSender
import android.os.ParcelUuid
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.core.content.ContextCompat.getSystemService
import java.util.*


class BluetoothPairing(private val context: Context) {

    private val deviceManager: CompanionDeviceManager by lazy {
        getSystemService(context, CompanionDeviceManager::class.java) as CompanionDeviceManager
    }


    fun startScan(launcher: ManagedActivityResultLauncher<IntentSenderRequest, ActivityResult>) {
        val deviceFilter: BluetoothDeviceFilter = BluetoothDeviceFilter.Builder()
            .addServiceUuid(ParcelUuid(MIDI_OVER_BTLE_UUID), null)
            .build()

        // The argument provided in setSingleDevice() determines whether a single
        // device name or a list of them appears.
        val pairingRequest: AssociationRequest = AssociationRequest.Builder()
            .addDeviceFilter(deviceFilter)
            .setSingleDevice(true)
            .build()

        // When the app tries to pair with a Bluetooth device, show the
        // corresponding dialog box to the user.
        deviceManager.associate(
            pairingRequest,
            object : CompanionDeviceManager.Callback() {

                override fun onAssociationPending(intentSender: IntentSender) {
//                    startIntentSenderForResult(intentSender,
//                        SELECT_DEVICE_REQUEST_CODE, null, 0, 0, 0)
                    launcher.launch(IntentSenderRequest.Builder(intentSender).build())

                }

                override fun onFailure(error: CharSequence?) {
                    // Handle the failure.
                }
            }, null
        )
    }


    companion object {
        private val MIDI_OVER_BTLE_UUID = UUID.fromString("03B80E5A-EDE8-4B33-A751-6CE34EC4C700")

        const val SELECT_DEVICE_REQUEST_CODE = 0

    }

}