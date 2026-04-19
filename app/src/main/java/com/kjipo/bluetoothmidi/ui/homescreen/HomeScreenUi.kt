package com.kjipo.bluetoothmidi

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kjipo.bluetoothmidi.ui.homescreen.HomeScreenModel
import com.kjipo.bluetoothmidi.ui.homescreen.HomeScreenModelUiState
import com.kjipo.bluetoothmidi.ui.homescreen.HomeState
import com.kjipo.bluetoothmidi.ui.sessionlist.MidiSessionData
import com.kjipo.bluetoothmidi.ui.sessionlist.getFormattedDuration
import java.time.format.DateTimeFormatter


@Composable
fun HomeRoute(homeScreenModel: HomeScreenModel, onStartSession: () -> Unit) {
    val uiState by homeScreenModel.uiState.collectAsState()

    HomeRouteScreen(
        uiState,
        { address -> homeScreenModel.attemptConnectToPreviouslyConnectedDevice(address) },
        onStartSession
    )

}


@Composable
fun HomeRouteScreen(
    homeScreenModelUiState: HomeScreenModelUiState,
    connectToLastConnectedDevice: (String) -> Unit,
    onStartSession: () -> Unit
) {
    val context = LocalContext.current
    val sharedPreferences = context.applicationContext.getSharedPreferences(
        PREFERENCES_KEY, android.content.Context.MODE_PRIVATE
    )
    val lastConnectedDeviceName = sharedPreferences.getString(LAST_CONNECTED_DEVICE_KEY, null)
    val lastConnectedDeviceAddress = sharedPreferences.getString(LAST_CONNECTED_DEVICE_ADDRESS, null)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Previously connected device:",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = lastConnectedDeviceName ?: "None",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        lastConnectedDeviceAddress?.let { connectToLastConnectedDevice(it) }
                    },
                    enabled = lastConnectedDeviceAddress != null && homeScreenModelUiState.state != HomeState.CONNECTING && homeScreenModelUiState.state != HomeState.CONNECTED,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Connect")
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Status:",
                    style = MaterialTheme.typography.titleMedium
                )

                val stateColor = when (homeScreenModelUiState.state) {
                    HomeState.STARTING -> Color.Gray
                    HomeState.CONNECTING -> Color(0xFF2196F3) // Blue
                    HomeState.CONNECTED -> Color(0xFF4CAF50) // Green
                    HomeState.FAILED_TO_CONNECT -> Color(0xFFF44336) // Red
                }

                Surface(
                    color = stateColor.copy(alpha = 0.1f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = homeScreenModelUiState.state.name,
                        color = stateColor,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Button(
            onClick = onStartSession,
            enabled = homeScreenModelUiState.state == HomeState.CONNECTED,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Start session")
        }

        PreviousSession(midiSessionData = homeScreenModelUiState.previousSession)
    }
}


@Composable
fun PreviousSession(midiSessionData: MidiSessionData?) {
    Text(
        text = "Last Session",
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        fontWeight = FontWeight.SemiBold
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            if (midiSessionData == null) {
                Text(
                    "No previous session recorded",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Date",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            DateTimeFormatter.ofPattern("MMM dd, yyyy").format(midiSessionData.start),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Duration",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            getFormattedDuration(midiSessionData),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}