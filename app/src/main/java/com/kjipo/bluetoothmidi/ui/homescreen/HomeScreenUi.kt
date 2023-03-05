package com.kjipo.bluetoothmidi

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.kjipo.bluetoothmidi.ui.homescreen.HomeScreenModel
import com.kjipo.bluetoothmidi.ui.homescreen.HomeScreenModelUiState
import com.kjipo.bluetoothmidi.ui.sessionlist.MidiSessionData
import com.kjipo.bluetoothmidi.ui.sessionlist.getFormattedDuration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


@Composable
fun HomeRoute(homeScreenModel: HomeScreenModel) {
    val uiState by homeScreenModel.uiState.collectAsState()

    HomeRouteScreen(
        homeRouteScreenInput = HomeRouteScreenInput(
            uiState,
            { homeScreenModel.connectedToLastConnectedDevice() })
    )

}

class HomeRouteScreenInput(
    val homeScreenModelUiState: HomeScreenModelUiState,
    val connectedToLastConnectedDevice: () -> Unit
)

class HomeRouteScreenInputParameterProvider : PreviewParameterProvider<HomeRouteScreenInput> {

    override val values = sequenceOf(HomeRouteScreenInput(
        HomeScreenModelUiState(
            "Test",
            previousSession = MidiSessionData(
                1,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now()
            )
        )
    ) {
        // Do nothing
    })

}

@Preview
@Composable
fun HomeRouteScreen(@PreviewParameter(HomeRouteScreenInputParameterProvider::class) homeRouteScreenInput: HomeRouteScreenInput) {
    Column {
        Text(
            text = "Home screen",
            modifier = Modifier
                .fillMaxWidth(),
            style = MaterialTheme.typography.h6,
            color = Color.Black
        )
        Text(
            text = "Previously connected device: ${homeRouteScreenInput.homeScreenModelUiState.previouslyConnectedDevice} (${homeRouteScreenInput.homeScreenModelUiState.connected})",
            modifier = Modifier
                .fillMaxWidth(),
            style = MaterialTheme.typography.h6,
            color = Color.Black
        )

        PreviousSession(midiSessionData = homeRouteScreenInput.homeScreenModelUiState.previousSession)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = homeRouteScreenInput.connectedToLastConnectedDevice,
                enabled = homeRouteScreenInput.homeScreenModelUiState.previouslyConnectedDevice.isNotEmpty()
            ) {
                Text(
                    text = "Connect",
                )
            }
        }

    }
}


@Composable
fun PreviousSession(midiSessionData: MidiSessionData?) {
    if (midiSessionData == null) {
        Text("No previous session")
        return
    }

    Text(
        DateTimeFormatter.ISO_DATE.format(midiSessionData.start),
        style = androidx.compose.material3.MaterialTheme.typography.titleMedium
    )
    Text(
        getFormattedDuration(midiSessionData),
        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
    )


}