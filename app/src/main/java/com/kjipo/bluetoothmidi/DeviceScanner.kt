package com.kjipo.bluetoothmidi

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.ParcelUuid
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import timber.log.Timber
import java.util.*

class DeviceScanner(private val applicationContext: Context) {

    private val scanFilter =
        ScanFilter.Builder().setServiceUuid(ParcelUuid(MIDI_OVER_BTLE_UUID)).build()


    private val bluetoothScanCallback = BluetoothScanCallback()

    private var isScanning = false

//    private val deviceListViewModel by viewModels<DeviceListViewModel> {
//        DeviceListViewModelFactory()
//    }


    private val foundDevices = MutableStateFlow<Set<BluetoothDeviceData>>(setOf())

    private val BluetoothAdapter.isDisabled: Boolean
        get() = !isEnabled

    private var bluetoothManager: BluetoothManager


    init {
        bluetoothManager = applicationContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    }


    private fun stopScanning() {
        bluetoothManager.adapter.bluetoothLeScanner.stopScan(bluetoothScanCallback)
        isScanning = false
    }


    private fun scanLeDevices(bluetoothAdapter: BluetoothAdapter) {
        if (isScanning) {
            // Already scanning
            return
        }
        val leScanner = bluetoothAdapter.bluetoothLeScanner
        isScanning = true

        Timber.i("Start scan")

        leScanner.startScan(
            listOf(scanFilter),
            ScanSettings.Builder().build(),
            bluetoothScanCallback
        )
    }

    fun observeDevices(): Flow<Set<BluetoothDeviceData>> = foundDevices

    fun toggleScan() {
        if (isScanning) {
            stopScanning()
        } else {
            val bluetoothManager =
                applicationContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            scanLeDevices(bluetoothManager.adapter)
        }
        isScanning != isScanning
    }

    inner class BluetoothScanCallback : ScanCallback() {

        override fun onScanResult(
            callbackType: Int,
            result: ScanResult?
        ) {
            Timber.i("Scan result. Callback type: ${callbackType}. Result: $result")

            result?.apply {
                BluetoothDeviceData(device).let { bluetoothDeviceData ->
//                    DeviceDataSource.getDataSource().insertDevice(it)
                    // TODO Need to think about which thread this is done on?
                    foundDevices.value = foundDevices.value.toMutableSet().apply {
                        add(bluetoothDeviceData)
                    }.toSet()

                }
            }
        }

        override fun onBatchScanResults(results: List<ScanResult?>?) {
            Timber.i("Scan results. Results: $results")

            results?.apply {
                filterNotNull().forEach {
                    BluetoothDeviceData(it.device).let { bluetoothDeviceData ->
//                    DeviceDataSource.getDataSource().insertDevice(bluetoothDeviceData)
                    }
                }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            isScanning = false
            Timber.e("Scan failed. Error code: $errorCode")
        }
    }

    companion object {

        private const val SCAN_PERIOD = 10000L
        private val MIDI_OVER_BTLE_UUID = UUID.fromString("03B80E5A-EDE8-4B33-A751-6CE34EC4C700")


    }

}
