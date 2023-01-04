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
import com.kjipo.bluetoothmidi.connect.ConnectViewModel
import com.kjipo.bluetoothmidi.connect.MidiDeviceConnectUiState

@Composable
fun ConnectRoute(connectViewModel: ConnectViewModel, navigateToHome: () -> Unit) {
    val uiState by connectViewModel.uiState.collectAsState()

    ConnectRoute(
        ConnectRouteInputHolder(
            {
                connectViewModel.closeSession()
                navigateToHome()
            },
            midiDeviceConnectUiState = uiState
        )
    )

}


class ConnectRouteInputHolder(
    val saveSessionCallback: () -> Unit,
    val midiDeviceConnectUiState: MidiDeviceConnectUiState
)


class ConnectRouteInputHolderProvider : PreviewParameterProvider<ConnectRouteInputHolder> {
    override val values = sequenceOf(
        ConnectRouteInputHolder(
            {
                // Do nothing
            },
            MidiDeviceConnectUiState(
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
fun ConnectRoute(@PreviewParameter(ConnectRouteInputHolderProvider::class) connectRouteInputHolder: ConnectRouteInputHolder) {

    Column {
        Row {
            Text("Connected: ${connectRouteInputHolder.midiDeviceConnectUiState.connected}")
        }
        Row {
            Text("Number of received messages: ${connectRouteInputHolder.midiDeviceConnectUiState.numberOfReceivedMessages}")
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Button(onClick = connectRouteInputHolder.saveSessionCallback) {
                Text("Close session")

            }
        }
    }

}
