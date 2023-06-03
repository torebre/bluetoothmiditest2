package com.kjipo.bluetoothmidi.ui.midiplay

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag


@Composable
fun PlayMidi(playViewModel: PlayViewModel) {
    val uiState = playViewModel.uiState.collectAsState()

    PlayMidi(
        uiState.value,
        onClickPlay = { playViewModel.play() },
        onClickPlayInput = { playViewModel.playInput() },
        onClickClearReceivedMessages = { playViewModel.clearReceivedMessages() }
    )

}


@Composable
fun PlayMidi(
    uiState: PlayViewUiState,
    onClickPlay: () -> Unit,
    onClickPlayInput: () -> Unit,
    onClickClearReceivedMessages: () -> Unit
) {

    Column {
        val modeText = when (uiState.playState) {
            PlayState.PLAYING -> "Playing"
            PlayState.USER_INPUT -> "User input"
            PlayState.PLAYING_USER_INPUT -> "Playing user input"
            PlayState.ERROR -> "Error"
        }

        Text("Mode: $modeText")

        Button(modifier = Modifier.semantics { testTag = "play" }, onClick = onClickPlay) {
            Text("Play")
        }

        Button(onClick = onClickPlayInput) {
            Text("Play input")
        }

        Button(onClick = onClickClearReceivedMessages) {
            Text("Clear received messages")
        }

        Text("Number of received messages: ${uiState.numberOfReceivedMessages}")

    }

}
