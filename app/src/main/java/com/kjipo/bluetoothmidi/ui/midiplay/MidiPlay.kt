package com.kjipo.bluetoothmidi.ui.midiplay

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember


@Composable
fun PlayMidi(playViewModel: PlayViewModel) {
    val uiState = playViewModel.uiState.collectAsState()
    val numberOfReceivedMessages = remember {
        uiState.value.numberOfReceivedMessages
    }

    PlayMidi(
        uiState.value.playState,
        onClickPlay = { playViewModel.play() },
        onClickPlayInput = { playViewModel.playInput() },
        onClickClearReceivedMessages = { playViewModel.clearReceivedMessages() },
        numberOfReceivedMessages
    )

}

@Composable
fun PlayMidi(
    mode: PlayState,
    onClickPlay: () -> Unit,
    onClickPlayInput: () -> Unit,
    onClickClearReceivedMessages: () -> Unit,
    numberOfReceivedMessages: Int
) {
    Column {
        val modeText = when (mode) {
            PlayState.PLAYING -> "Playing"
            PlayState.USER_INPUT -> "User input"
            PlayState.WAITING -> "Waiting"
            PlayState.PLAYING_USER_INPUT -> "Playing user input"
        }

        Text("Mode: $modeText")

        Button(onClick = onClickPlay) {
            Text("Play")
        }

        Button(onClick = onClickPlayInput) {
            Text("Play input")
        }

        Button(onClick = onClickClearReceivedMessages) {
            Text("Clear received messages")
        }

        Text("Number of received messages: $numberOfReceivedMessages")


    }

}
