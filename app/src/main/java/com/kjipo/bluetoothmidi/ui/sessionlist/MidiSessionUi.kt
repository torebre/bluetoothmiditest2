package com.kjipo.bluetoothmidi.ui.sessionlist

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.Text
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


data class MidiSessionData(
    val midiSessionId: Long,
    val start: LocalDateTime,
    val stop: LocalDateTime
)

class MidiSessionUiInput(
    val midiSessionUiState: StateFlow<MidiSessionUiState>,
    val navigateToSessionInformation: (Long) -> Unit
)

class MidiSessionUiParameterProvider : PreviewParameterProvider<MidiSessionUiInput> {

    override val values = sequenceOf(
        MidiSessionUiInput(
            MutableStateFlow(
                MidiSessionUiState(
                    listOf(
                        MidiSessionData(
                            1,
                            LocalDateTime.now().minusDays(4),
                            LocalDateTime.now().minusDays(3)
                        ),
                        MidiSessionData(
                            2,
                            LocalDateTime.now().minusDays(3),
                            LocalDateTime.now().minusDays(2)
                        ),
                        MidiSessionData(
                            3,
                            LocalDateTime.now().minusDays(2),
                            LocalDateTime.now().minusDays(1)
                        )
                    )
                )
            ),
            { sessionId: Long ->
                // Do nothing
            }
        ))

}

@Preview
@Composable
fun MidiSessionUi(
    @PreviewParameter(MidiSessionUiParameterProvider::class) midiSessionUiInputData: MidiSessionUiInput) {
    val sessionDataUi by midiSessionUiInputData.midiSessionUiState.collectAsState()
    val selectedSession = remember {
        mutableStateOf(0L)
    }

    Column {
        sessionDataUi.storedSessions.forEach { midiSessionData ->
            SessionEntry(SessionEntryData(midiSessionData, selectedSession, midiSessionUiInputData.navigateToSessionInformation))
        }
    }

}

class SessionEntryData(
    val midiSessionData: MidiSessionData,
    val selectedMidiSession: MutableState<Long>,
    val navigateToSessionInformation: (Long) -> Unit
)

class SessionEntryDataProvider : PreviewParameterProvider<SessionEntryData> {
    override val values = sequenceOf(
        SessionEntryData(
            MidiSessionData(
                1,
                LocalDateTime.now().minusDays(2),
                LocalDateTime.now().minusDays(1)
            ),
            mutableStateOf(1),
            { sessionId ->
                // Do nothing
            }
        )
    )
}

@Composable
fun SessionEntry(sessionEntryData: SessionEntryData) {
    Column {
        LazyRow(
            Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, top = 2.dp)
                .selectable(sessionEntryData.midiSessionData.midiSessionId == sessionEntryData.selectedMidiSession.value,
                    onClick = {
                        // TODO Enable selection again
//                        sessionEntryData.selectedMidiSession.value =
//                            sessionEntryData.midiSessionData.midiSessionId
                        sessionEntryData.navigateToSessionInformation(sessionEntryData.midiSessionData.midiSessionId)
                    })
        ) {
            item {
                Column(modifier = Modifier.padding(start = 2.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp, bottom = 2.dp)
                    ) {
                        Text(
                            DateTimeFormatter.ISO_DATE.format(sessionEntryData.midiSessionData.start),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    Row {
                        Text(
                            getFormattedDuration(sessionEntryData),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
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


private fun getFormattedDuration(sessionEntryData: SessionEntryData): String {
    with(
        Duration.between(
            sessionEntryData.midiSessionData.start,
            sessionEntryData.midiSessionData.stop
        )
    ) {
        return "${toTwoDigits(toHoursPartHelper())}:${toTwoDigits(toMinutesPartHelper())}:${
            toTwoDigits(
                toSecondsPartHelper()
            )
        }"
    }
}

