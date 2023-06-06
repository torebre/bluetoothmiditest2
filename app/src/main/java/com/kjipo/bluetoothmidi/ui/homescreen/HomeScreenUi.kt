package com.kjipo.bluetoothmidi

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.kjipo.bluetoothmidi.ui.homescreen.HomeScreenModel
import com.kjipo.bluetoothmidi.ui.homescreen.HomeScreenModelUiState
import com.kjipo.bluetoothmidi.ui.homescreen.HomeState
import com.kjipo.bluetoothmidi.ui.sessionlist.MidiSessionData
import com.kjipo.bluetoothmidi.ui.sessionlist.getFormattedDuration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


@Composable
fun HomeRoute(homeScreenModel: HomeScreenModel) {
    val uiState by homeScreenModel.uiState.collectAsState()

    HomeRouteScreen(
        uiState,
        { address -> homeScreenModel.attemptConnectToPreviouslyConnectedDevice(address) }
    )

}


@Composable
fun AttemptConnection(
    homeScreenModelUiState: HomeScreenModelUiState,
    connectedToLastConnectedDevice: (String) -> Unit
) {
    if (homeScreenModelUiState.state == HomeState.STARTING) {
        LocalContext.current.applicationContext.getSharedPreferences(
            PREFERENCES_KEY, Context.MODE_PRIVATE
        ).getString(LAST_CONNECTED_DEVICE_ADDRESS, null)?.let {
            connectedToLastConnectedDevice(it)
        }
    }
}

@Composable
fun HomeRouteScreen(
    homeScreenModelUiState: HomeScreenModelUiState,
    connectedToLastConnectedDevice: (String) -> Unit
) {
    AttemptConnection(
        homeScreenModelUiState = homeScreenModelUiState,
        connectedToLastConnectedDevice = connectedToLastConnectedDevice
    )

    val lastConnectedDevice = LocalContext.current.applicationContext.getSharedPreferences(
        PREFERENCES_KEY, Context.MODE_PRIVATE
    ).getString(LAST_CONNECTED_DEVICE_KEY, null)

    Column {
        Text(
            text = "Home screen",
            modifier = Modifier
                .fillMaxWidth(),
            style = MaterialTheme.typography.h6,
            color = Color.Black
        )
        Text(
            text = "Previously connected device: $lastConnectedDevice",
            modifier = Modifier
                .fillMaxWidth(),
            style = MaterialTheme.typography.h6,
            color = Color.Black
        )

        PreviousSession(midiSessionData = homeScreenModelUiState.previousSession)

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