package com.kjipo.bluetoothmidi.ui.sessionlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.Alignment
import com.kjipo.bluetoothmidi.MidiSessionUiState
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


data class MidiSessionData(
    val midiSessionId: Long,
    val start: LocalDateTime,
    val stop: LocalDateTime
)

class MidiSessionUiInput(
    val navigateToSessionInformation: (Long) -> Unit,
    val exportSessions: (Collection<Long>) -> Unit
)

@Composable
fun MidiSessionUi(
    midiSessionUiState: MidiSessionUiState,
    midiSessionUiInputData: MidiSessionUiInput
) {
    val selectedSessions = remember {
        mutableStateOf(setOf<Long>())
    }

    Column {
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(midiSessionUiState.storedSessions) { midiSessionData ->
                SessionEntry(
                    SessionEntryData(
                        midiSessionData,
                        selectedSessions.value.contains(midiSessionData.midiSessionId),
                        midiSessionUiInputData.navigateToSessionInformation
                    ) {
                        if (selectedSessions.value.contains(midiSessionData.midiSessionId)) {
                            selectedSessions.value -= midiSessionData.midiSessionId

                        } else {
                            selectedSessions.value += midiSessionData.midiSessionId
                        }
                    }
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            ExportSessions(selectedSessions.value.isNotEmpty()) {
                midiSessionUiInputData.exportSessions(selectedSessions.value)
            }
        }
    }
}

class SessionEntryData(
    val midiSessionData: MidiSessionData,
    val selected: Boolean,
    val navigateToSessionInformation: (Long) -> Unit,
    val toggleSelection: () -> Unit
)

@Composable
fun SessionEntry(sessionEntryData: SessionEntryData) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = sessionEntryData.selected,
                onCheckedChange = { sessionEntryData.toggleSelection() }
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                        .format(sessionEntryData.midiSessionData.start),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Duration: ${getFormattedDuration(sessionEntryData.midiSessionData)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = {
                sessionEntryData.navigateToSessionInformation(sessionEntryData.midiSessionData.midiSessionId)
            }) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Session details",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}


@Composable
fun ExportSessions(enabled: Boolean, exportSessions: () -> Unit) {
//    val startSenderForResult = ActivityResultContracts.StartIntentSenderForResult()

//    val launcher =
//        rememberLauncherForActivityResult(contract = startSenderForResult) { activityResult ->
//            when (activityResult.resultCode) {
//                Activity.RESULT_OK -> {
//                    val deviceToPair: BluetoothDevice? =
//                        activityResult.data?.getParcelableExtra(CompanionDeviceManager.EXTRA_DEVICE)
//                    deviceToPair?.createBond()
//                }
//
//            }
//
//        }


    Button(enabled = enabled, onClick = exportSessions) {
        Text("Export")
    }
}

fun Duration.toHoursPartHelper(): Int {
    return (toHours() % 24).toInt()
}

fun Duration.toMinutesPartHelper(): Int {
    return (toMinutes() % 60).toInt()
}

fun Duration.toSecondsPartHelper(): Int {
    return (seconds % 60).toInt()
}

private fun toTwoDigits(value: Int): String {
    return if (value < 10) {
        "0${value}"
    } else {
        "$value"
    }
}


fun getFormattedDuration(midiSessionData: MidiSessionData): String {
    return getFormattedDuration(midiSessionData.start, midiSessionData.stop)
}

fun getFormattedDuration(start: LocalDateTime, stop: LocalDateTime): String {
    with(
        Duration.between(start, stop)
    ) {
        return "${toTwoDigits(toHoursPartHelper())}:${toTwoDigits(toMinutesPartHelper())}:${
            toTwoDigits(
                toSecondsPartHelper()
            )
        }"
    }
}
