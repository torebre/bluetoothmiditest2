package com.kjipo.bluetoothmidi.ui.sessionlist

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.kjipo.bluetoothmidi.MidiSessionUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToLong


data class MidiSessionData(
    val midiSessionId: Long,
    val start: LocalDateTime,
    val stop: LocalDateTime
)

class MidiSessionUiInput(
    val midiSessionUiState: StateFlow<MidiSessionUiState>,
    val navigateToSessionInformation: (Long) -> Unit,
    val exportSessions: (Collection<Long>) -> Unit
)

class MidiSessionUiParameterProvider : PreviewParameterProvider<MidiSessionUiInput> {

    override val values = sequenceOf(
        MidiSessionUiInput(
            MutableStateFlow(
                MidiSessionUiState(
                    createSessions()
                )
            ),
            {
                // Do nothing
            },
            {
                // Do nothing
            }
        ))


    private fun createSessions(): List<MidiSessionData> {
        return (1L..10L).map { id ->
            val stop = (Math.random() * 10).roundToLong()
            val start = stop - (Math.random() * 2).roundToLong()
            MidiSessionData(
                id,
                LocalDateTime.now().minusDays(start),
                LocalDateTime.now().minusDays(stop)
            )
        }.toList()
    }

}

@Preview
@Composable
fun MidiSessionUi(
    @PreviewParameter(MidiSessionUiParameterProvider::class) midiSessionUiInputData: MidiSessionUiInput
) {
    val sessionDataUi by midiSessionUiInputData.midiSessionUiState.collectAsState()
    val selectedSessions = remember {
        mutableStateOf(setOf<Long>())
    }
//    val openDocumentTreeLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocumentTree(),
//        onResult = { uri ->
//            uri?.let {
//                midiSessionUiInputData.exportSessions(selectedSessions.value, it)
//            }
//        })


    Column {
        Row {
            sessionDataUi.storedSessions.forEach { midiSessionData ->
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
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            ExportSessions(selectedSessions.value.isNotEmpty()) {
//                openDocumentTreeLauncher.launch(null)
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

class SessionEntryDataProvider : PreviewParameterProvider<SessionEntryData> {
    override val values = sequenceOf(
        SessionEntryData(
            MidiSessionData(
                1,
                LocalDateTime.now().minusDays(2),
                LocalDateTime.now().minusDays(1)
            ),
            true,
            {
                // Do nothing
            },
            {
                // Do nothing
            }
        )
    )
}

@Preview
@Composable
fun SessionEntry(@PreviewParameter(SessionEntryDataProvider::class) sessionEntryData: SessionEntryData) {
    Column {
        LazyRow(
            Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, top = 2.dp)
        ) {
            item {
                Column {
                    Checkbox(checked = sessionEntryData.selected,
                        onCheckedChange = {
                            sessionEntryData.toggleSelection()
                        })
                }
                Column(modifier = Modifier.padding(start = 2.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp, bottom = 2.dp)
                    ) {
                        Column {
                            Text(
                                DateTimeFormatter.ISO_DATE.format(sessionEntryData.midiSessionData.start),
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                getFormattedDuration(sessionEntryData.midiSessionData),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Column {
                            IconButton(
                                onClick = {
                                    sessionEntryData.navigateToSessionInformation(sessionEntryData.midiSessionData.midiSessionId)
                                }) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Session details"
                                )
                            }
                        }
                    }
                }
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

