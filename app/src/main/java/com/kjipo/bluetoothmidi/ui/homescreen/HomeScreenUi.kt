package com.kjipo.bluetoothmidi

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
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

    override val values = sequenceOf(HomeRouteScreenInput(HomeScreenModelUiState("Test")
    ) {
        // Do nothing
    })

}

@Preview
@Composable
fun HomeRouteScreen(@PreviewParameter(HomeRouteScreenInputParameterProvider::class) homeRouteScreenInput: HomeRouteScreenInput) {
    Column {
        Row {
            Text(
                text = "Home screen",
                modifier = Modifier
                    .fillMaxWidth(),
                style = MaterialTheme.typography.h6,
                color = Color.Black
            )
        }
        Row {
            Text(
                text = "Previously connected device: ${homeRouteScreenInput.homeScreenModelUiState.previouslyConnectedDevice} (${homeRouteScreenInput.homeScreenModelUiState.connected})",
                modifier = Modifier
                    .fillMaxWidth(),
                style = MaterialTheme.typography.h6,
                color = Color.Black
            )
        }
        Row {
            Button(onClick = homeRouteScreenInput.connectedToLastConnectedDevice, enabled = homeRouteScreenInput.homeScreenModelUiState.previouslyConnectedDevice.isNotEmpty()) {
                Text(
                    text = "Connect",
                )
            }
            }
    }
}