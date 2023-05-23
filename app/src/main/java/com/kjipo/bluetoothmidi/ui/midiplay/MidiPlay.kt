package com.kjipo.bluetoothmidi.ui.midiplay

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState


@Composable
fun PlayMidi(playViewModel: PlayViewModel) {
    val uiState = playViewModel.uiState.collectAsState()

    PlayMidi(uiState.value.playState,
        onClickPlay = { playViewModel.play() },
        onClickPlayInput = {playViewModel.playInput() })

}

@Composable
fun PlayMidi(mode: PlayState,
             onClickPlay: () -> Unit,
onClickPlayInput: () -> Unit) {
    Column {
        val modeText = when(mode) {
            PlayState.PLAYING -> "Playing"
            PlayState.USER_INPUT -> "User input"
            PlayState.WAITING -> "Waiting"
        }

        Text("Mode: $modeText")

        Button(onClick = onClickPlay) {
            Text("Play")
        }

        Button(onClick = onClickPlayInput) {
            Text("Play input")
        }

    }

}
