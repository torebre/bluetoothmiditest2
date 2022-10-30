package com.kjipo.bluetoothmidi.devicelist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kjipo.bluetoothmidi.BluetoothDeviceData
import com.kjipo.bluetoothmidi.DeviceScanner
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber

class DeviceListViewModel(private val deviceScanner: DeviceScanner) : ViewModel() {

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


    companion object {

        fun provideFactory(deviceScanner: DeviceScanner): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return DeviceListViewModel(deviceScanner) as T
                }
            }

    }

}


class MidiDevicesUiState(val isScanning: Boolean, val foundDevices: List<BluetoothDeviceData>)


private data class DeviceListViewModelState(
    val isScanning: Boolean = false,
    val foundDevices: Set<BluetoothDeviceData> = emptySet()
) {

    fun toUiState(): MidiDevicesUiState {
        return MidiDevicesUiState(isScanning, foundDevices.sortedBy { it.name })
    }

}