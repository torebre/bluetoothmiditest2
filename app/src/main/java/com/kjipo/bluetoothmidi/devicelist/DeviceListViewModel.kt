package com.kjipo.bluetoothmidi.devicelist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kjipo.bluetoothmidi.BluetoothDeviceData
import com.kjipo.bluetoothmidi.DeviceScanner
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class DeviceListViewModel(private val deviceScanner: DeviceScanner): ViewModel() {
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


    companion object {

        fun provideFactory(deviceScanner: DeviceScanner): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return DeviceListViewModel(deviceScanner) as T
            }
        }

    }

}


class  MidiDevicesUiState(val isScanning: Boolean, val foundDevices: List<BluetoothDeviceData>)


private data class DeviceListViewModelState(val foundDevices: Set<BluetoothDeviceData> = emptySet()) {



    fun toUiState(): MidiDevicesUiState {
        // TODO Set scanning state correctly
       return MidiDevicesUiState(false, foundDevices.sortedBy { it.bluetoothDevice.name })

    }



}