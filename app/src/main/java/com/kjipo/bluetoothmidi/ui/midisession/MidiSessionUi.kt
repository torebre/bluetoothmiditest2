package com.kjipo.bluetoothmidi.ui.midisession

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.kjipo.bluetoothmidi.connect.MidiSessionViewModel
import com.kjipo.bluetoothmidi.connect.MidiSessionUiState

@Composable
fun MidiSessionRoute(midiSessionViewModel: MidiSessionViewModel, navigateToHome: () -> Unit) {
    val uiState by midiSessionViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        midiSessionViewModel.startSession()
    }

    MidiSessionRoute(
        MidiSessionRouteInputHolder(
            {
                midiSessionViewModel.closeSession()
                navigateToHome()
            },
            midiDeviceConnectUiState = uiState
        )
    )

}


class MidiSessionRouteInputHolder(
    val saveSessionCallback: () -> Unit,
    val midiDeviceConnectUiState: MidiSessionUiState
)


@Composable
fun MidiSessionRoute(midiSessionRouteInputHolder: MidiSessionRouteInputHolder) {

    Column {
        Text("Connected: ${midiSessionRouteInputHolder.midiDeviceConnectUiState.connected}")
        Text("Number of received messages: ${midiSessionRouteInputHolder.midiDeviceConnectUiState.numberOfReceivedMessages}")
        if (midiSessionRouteInputHolder.midiDeviceConnectUiState.closingSession) {
            Text("Closing session")
        } else {
            Text("Session duration: ${midiSessionRouteInputHolder.midiDeviceConnectUiState.sessionDurationInSeconds}")
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Button(onClick = midiSessionRouteInputHolder.saveSessionCallback) {
                Text("Close session")
            }
        }
    }

}
