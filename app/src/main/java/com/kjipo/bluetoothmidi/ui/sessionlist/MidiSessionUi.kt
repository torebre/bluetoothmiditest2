package com.kjipo.bluetoothmidi.ui.sessionlist

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.kjipo.bluetoothmidi.MidiSessionUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


data class MidiSessionData(
    val midiSessionId: Int,
    val start: LocalDateTime,
    val stop: LocalDateTime
)

//class MidiSessionUiInputData(val storedSessions: List<MidiSessionData>)
//class MidiSessionUiInputData(val storedSessions: List<MidiSessionData>)


class MidiSessionUiParameterProvider : PreviewParameterProvider<StateFlow<MidiSessionUiState>> {

//    override val values = sequenceOf(
//        MidiSessionUiInputData(
//            listOf(
//                MidiSessionData(
//                    1,
//                    LocalDateTime.now().minusDays(2),
//                    LocalDateTime.now().minusDays(1)
//                )
//            )
//        )
//    )

    override val values = sequenceOf(
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
        )
    )

}

@Preview
@Composable
fun MidiSessionUi(@PreviewParameter(MidiSessionUiParameterProvider::class) midiSessionUiInputData: StateFlow<MidiSessionUiState>) {
    val sessionDataUi by midiSessionUiInputData.collectAsState()
    val selectedSession = remember {
        mutableStateOf(0)
    }

    Column {
            sessionDataUi.storedSessions.forEach { midiSessionData ->
                SessionEntry(SessionEntryData(midiSessionData, selectedSession))
            }
    }

}

class SessionEntryData(
    val midiSessionData: MidiSessionData,
    val selectedMidiSession: MutableState<Int>
)

class SessionEntryDataProvider : PreviewParameterProvider<SessionEntryData> {
    override val values = sequenceOf(
        SessionEntryData(
            MidiSessionData(
                1,
                LocalDateTime.now().minusDays(2),
                LocalDateTime.now().minusDays(1)
            ),
            mutableStateOf(1)
        )
    )
}

@Composable
fun SessionEntry(sessionEntryData: SessionEntryData) {
    Column {
        LazyRow(
            Modifier
                .fillMaxWidth()
                .selectable(sessionEntryData.midiSessionData.midiSessionId == sessionEntryData.selectedMidiSession.value,
                    onClick = {
                        sessionEntryData.selectedMidiSession.value =
                            sessionEntryData.midiSessionData.midiSessionId
                    })
        ) {
            item {
                Text(DateTimeFormatter.ISO_DATE_TIME.format(sessionEntryData.midiSessionData.start))
                Text(DateTimeFormatter.ISO_DATE_TIME.format(sessionEntryData.midiSessionData.stop))
            }
        }
    }
}