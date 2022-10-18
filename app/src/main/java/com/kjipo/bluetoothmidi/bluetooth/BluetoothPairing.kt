package com.kjipo.bluetoothmidi.bluetooth

import android.companion.AssociationRequest
import android.companion.BluetoothDeviceFilter
import android.companion.CompanionDeviceManager
import android.content.Context
import android.content.IntentSender
import android.os.ParcelUuid
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.core.content.ContextCompat.getSystemService
import timber.log.Timber
import java.util.*


class BluetoothPairing(private val context: Context) {

    private val deviceManager: CompanionDeviceManager by lazy {
        getSystemService(context, CompanionDeviceManager::class.java) as CompanionDeviceManager
    }


    fun startScan(launcher: ManagedActivityResultLauncher<IntentSenderRequest, ActivityResult>) {
        Timber.i("Start scan")
        Log.i("Bluetooth", "Start scan")

        val deviceFilter: BluetoothDeviceFilter = BluetoothDeviceFilter.Builder()
            .addServiceUuid(ParcelUuid(MIDI_OVER_BTLE_UUID), null)
            .build()

        // The argument provided in setSingleDevice() determines whether a single
        // device name or a list of them appears.
        val pairingRequest: AssociationRequest = AssociationRequest.Builder()
            .addDeviceFilter(deviceFilter)
            .setSingleDevice(false)
            .build()

        // When the app tries to pair with a Bluetooth device, show the
        // corresponding dialog box to the user.
        deviceManager.associate(
            pairingRequest,
            object : CompanionDeviceManager.Callback() {

                override fun onDeviceFound(intentSender: IntentSender) {
                    // Need to override the deprecated onDeviceFound-method since that is what the API version on my phone is using
                    onAssociationPending(intentSender)
                }

                override fun onAssociationPending(intentSender: IntentSender) {
//                    startIntentSenderForResult(intentSender,
//                        SELECT_DEVICE_REQUEST_CODE, null, 0, 0, 0)

                    Timber.i("Association pending")

                    launcher.launch(IntentSenderRequest.Builder(intentSender).build())

                }

                override fun onFailure(error: CharSequence?) {
                    // Handle the failure.
                    error.toString().let {
                        Timber.tag("Bluetooth").e(it)

                        Log.e("Bluetooth2", it)
                    }
                }
            }, null
        )
    }


    companion object {
        private val MIDI_OVER_BTLE_UUID = UUID.fromString("03B80E5A-EDE8-4B33-A751-6CE34EC4C700")

        const val SELECT_DEVICE_REQUEST_CODE = 0

    }

}