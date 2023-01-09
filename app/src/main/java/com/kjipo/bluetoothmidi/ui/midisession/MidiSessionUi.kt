package com.kjipo.bluetoothmidi

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.kjipo.bluetoothmidi.connect.MidiSessionViewModel
import com.kjipo.bluetoothmidi.connect.MidiSessionUiState

@Composable
fun MidiSessionRoute(midiSessionViewModel: MidiSessionViewModel, navigateToHome: () -> Unit) {
    val uiState by midiSessionViewModel.uiState.collectAsState()

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


class ConnectRouteInputHolderProvider : PreviewParameterProvider<MidiSessionRouteInputHolder> {
    override val values = sequenceOf(
        MidiSessionRouteInputHolder(
            {
                // Do nothing
            },
            MidiSessionUiState(
                "",
                false,
                0,
                false
            )
        )
    )
}

@Preview(showBackground = true)
@Composable
fun MidiSessionRoute(@PreviewParameter(ConnectRouteInputHolderProvider::class) midiSessionRouteInputHolder: MidiSessionRouteInputHolder) {

    Column {
        Row {
            Text("Connected: ${midiSessionRouteInputHolder.midiDeviceConnectUiState.connected}")
        }
        Row {
            Text("Number of received messages: ${midiSessionRouteInputHolder.midiDeviceConnectUiState.numberOfReceivedMessages}")
        }
        Row {
            Text("Session duration: ${midiSessionRouteInputHolder.midiDeviceConnectUiState.sessionDurationInSeconds}")
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Button(onClick = midiSessionRouteInputHolder.saveSessionCallback) {
                Text("Close session")
            }
        }
    }

}
