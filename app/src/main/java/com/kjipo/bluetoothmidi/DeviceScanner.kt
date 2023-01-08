package com.kjipo.bluetoothmidi

import android.Manifest.permission.BLUETOOTH_CONNECT
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.ParcelUuid
import androidx.core.app.ActivityCompat
import com.kjipo.bluetoothmidi.midi.MidiHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import timber.log.Timber
import java.util.*

class DeviceScanner(private val applicationContext: Context) {

    private val scanFilter =
        ScanFilter.Builder().setServiceUuid(ParcelUuid(MIDI_OVER_BTLE_UUID)).build()


    private var bluetoothScanCallback: BluetoothScanCallback? = null

    private var isScanning = false

//    private val deviceListViewModel by viewModels<DeviceListViewModel> {
//        DeviceListViewModelFactory()
//    }


    private val foundDevices: MutableStateFlow<Set<BluetoothDeviceData>>

    private val BluetoothAdapter.isDisabled: Boolean
        get() = !isEnabled

    private val bluetoothManager: BluetoothManager


    init {
        bluetoothManager =
            applicationContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

        val bondedDevices =
            bluetoothManager.adapter.bondedDevices.filter { bondedBluetoothDevice ->
                Timber.tag("Bluetooth").i("Bluetooth device: ${bondedBluetoothDevice.name}")
                if (bondedBluetoothDevice.uuids == null) {
                    Timber.tag("Bluetooth").i("No UUIDs")
                    true
                } else {
                    bondedBluetoothDevice.uuids.firstOrNull {
                        Timber.tag("Bluetooth").i("ParcelUuid: $it")

                        it.equals(MIDI_OVER_BTLE_UUID)
                    } != null
                }
            }.map {
                BluetoothDeviceData(
                    it.name,
                    it.address,
                    it.bondState
                )
            }.toSet()
        foundDevices = MutableStateFlow(bondedDevices)
    }


    private fun stopScanning() {
        bluetoothManager.adapter.bluetoothLeScanner.stopScan(bluetoothScanCallback)
        bluetoothScanCallback = null

        Timber.tag("Bluetooth").i("Stopping scanning")
    }


    private fun scanLeDevices(bluetoothAdapter: BluetoothAdapter) {
        val leScanner = bluetoothAdapter.bluetoothLeScanner
        bluetoothScanCallback = BluetoothScanCallback()

        Timber.tag("Bluetooth").i("Start scan: ${isScanning}")

        // TODO Not necessary to callback for all matches
        leScanner.startScan(
            listOf(scanFilter),
//            emptyList<ScanFilter>(),
            ScanSettings.Builder().setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES).build(),
            bluetoothScanCallback
        )

        Timber.tag("Bluetooth").i("Enabled: ${bluetoothAdapter.isEnabled}")
    }

    fun observeDevices(): Flow<Set<BluetoothDeviceData>> = foundDevices

    fun toggleScan(): Boolean {
        Timber.tag("Bluetooth").i("Old scanning status: $isScanning")

        if (isScanning) {
            stopScanning()
        } else {
            scanLeDevices(bluetoothManager.adapter)
        }
        isScanning = !isScanning

        return isScanning
    }

    fun scanStatus() = isScanning



    inner class BluetoothScanCallback : ScanCallback() {

        override fun onScanResult(
            callbackType: Int,
            result: ScanResult?
        ) {
            Timber.tag("Bluetooth")
                .i("Scan result. Callback type: ${callbackType}. Result: $result")

            result?.apply {
                if (ActivityCompat.checkSelfPermission(
                        applicationContext,
                        BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    Timber.tag("Bluetooth")
                        .e("Expected $BLUETOOTH_CONNECT permission to have been given")
                } else {
                    BluetoothDeviceData(
                        device.name,
                        device.address,
                        device.bondState
                    ).let { bluetoothDeviceData ->
//                    DeviceDataSource.getDataSource().insertDevice(it)
                        // TODO Need to think about which thread this is done on?
                        foundDevices.value = foundDevices.value.toMutableSet().apply {
                            add(bluetoothDeviceData)
                        }.toSet()
                    }
                }

            }
        }

        override fun onBatchScanResults(results: List<ScanResult?>?) {
            Timber.tag("Bluetooth").i("Scan results. Results: $results")

            results?.apply {
                filterNotNull().forEach { scanResult ->
                    BluetoothDeviceData(
                        scanResult.device.name,
                        scanResult.device.address,
                        scanResult.device.bondState
                    ).let { bluetoothDeviceData ->

                        foundDevices.value = foundDevices.value.toMutableSet().apply {
                            add(bluetoothDeviceData)
                        }.toSet()
                    }
                }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            isScanning = false
            Timber.tag("Bluetooth").e("Scan failed. Error code: $errorCode")
        }
    }

    companion object {
        private const val SCAN_PERIOD = 10000L
        private val MIDI_OVER_BTLE_UUID = UUID.fromString("03B80E5A-EDE8-4B33-A751-6CE34EC4C700")


    }

}
