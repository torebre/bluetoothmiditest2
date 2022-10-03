package com.kjipo.bluetoothmidi.ui.midirecord

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import com.kjipo.bluetoothmidi.BluetoothDeviceData
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import com.kjipo.bluetoothmidi.DeviceScanner
import com.kjipo.bluetoothmidi.devicelist.MidiDevicesUiState


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowDevices(uiState: MidiDevicesUiState) {
    val topAppBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(topAppBarState)


    MidiDeviceList(uiState.foundDevices)



}