package com.kjipo.bluetoothmidi.ui.homescreen

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kjipo.bluetoothmidi.LAST_CONNECTED_DEVICE_ADDRESS
import com.kjipo.bluetoothmidi.LAST_CONNECTED_DEVICE_KEY
import com.kjipo.bluetoothmidi.midi.MidiHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber


class HomeScreenModel(private val midiHandler: MidiHandler, preferences: SharedPreferences) :
    ViewModel() {

    private val lastConnectedDevice = preferences.getString(LAST_CONNECTED_DEVICE_KEY, null)
    private val lastConnectedDeviceAddress =
        preferences.getString(LAST_CONNECTED_DEVICE_ADDRESS, null)

    private val viewModelState = MutableStateFlow(HomeScreenModelUiState(lastConnectedDevice ?: ""))

    val uiState =
        viewModelState.stateIn(viewModelScope, SharingStarted.Eagerly, viewModelState.value)

    init {
        Timber.tag("Bluetooth").i("Last connected device address: $lastConnectedDeviceAddress")
    }


    fun connectedToLastConnectedDevice() {
        if (lastConnectedDevice == null) {
            return
        }

        lastConnectedDeviceAddress?.let { address ->
            viewModelState.update { it.copy(isConnecting = true, connected = false) }

            viewModelScope.launch {
                Timber.tag("Bluetooth").i("Opening device with address: $address")
                if (midiHandler.openDevice(address)) {
                    Timber.tag("Bluetooth").i("Opened device")
                    viewModelState.update {
                        it.copy(
                            isConnecting = false,
                            connected = true
                        )
                    }
                } else {
                    Timber.tag("Bluetooth").i("Failed to open device")
                    viewModelState.update {
                        it.copy(
                            isConnecting = false,
                            connected = false
                        )
                    }
                }
            }
        }
    }

    companion object {

        fun provideFactory(
            midiHandler: MidiHandler,
            preferences: SharedPreferences
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return HomeScreenModel(midiHandler, preferences) as T
                }
            }
    }

}

data class HomeScreenModelUiState(
    val previouslyConnectedDevice: String = "",
    val isConnecting: Boolean = false,
    val connected: Boolean = false
)