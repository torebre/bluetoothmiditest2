package com.kjipo.bluetoothmidi.devicelist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kjipo.bluetoothmidi.BluetoothDeviceData
import com.kjipo.bluetoothmidi.DeviceScanner
import com.kjipo.bluetoothmidi.midi.MidiHandler
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber

class DeviceListViewModel(private val deviceScanner: DeviceScanner, private val midiHandler: MidiHandler) : ViewModel() {

    private val viewModelState = MutableStateFlow(DeviceListViewModelState())

    val uiState = viewModelState.map {
        it.toUiState()
    }.stateIn(viewModelScope, SharingStarted.Eagerly, viewModelState.value.toUiState())


    init {
        viewModelScope.launch {
            deviceScanner.observeDevices().collect { devices ->
                viewModelState.update { it.copy(foundDevices = devices) }
            }
        }
    }

    fun toggleScan() {
        viewModelScope.launch {
            // TODO Should these methods only be called inside this coroutinescope?
            val scanStatus = deviceScanner.toggleScan()

            Timber.tag("Bluetooth").i("Returned from scanning. Scan status: $scanStatus")

            viewModelState.update { it.copy(isScanning = scanStatus) }
        }

    }

    fun connectToDevice(address: String) {
        viewModelState.update { it.copy(isConnecting = true) }

        if(midiHandler.openDevice(address)) {
            viewModelState.update { it.copy(isConnecting = true, connectedDeviceAddress = address) }
        }
        else {
            viewModelState.update { it.copy(isConnecting = true) }
        }

        viewModelState.update { it.copy(isConnecting = false) }
    }

    companion object {

        fun provideFactory(deviceScanner: DeviceScanner, midiHandler: MidiHandler): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return DeviceListViewModel(deviceScanner, midiHandler) as T
                }
            }
    }

}


class MidiDevicesUiState(val isScanning: Boolean, val foundDevices: List<BluetoothDeviceData>, val connectedDeviceAddress: String?)


private data class DeviceListViewModelState(
    val isScanning: Boolean = false,
    val foundDevices: Set<BluetoothDeviceData> = emptySet(),
    val isConnecting: Boolean = false,
    val connectedDeviceAddress: String? = null
) {

    fun toUiState(): MidiDevicesUiState {
        return MidiDevicesUiState(isScanning, foundDevices.sortedBy { it.name }, connectedDeviceAddress = connectedDeviceAddress)
    }

}