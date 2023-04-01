package com.kjipo.bluetoothmidi.ui.midiplay

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable


@Composable
fun PlayMidi(onClickPlay: () -> Unit) {
    Column {
        Button(onClick = onClickPlay) {
            Text("Play")
        }

    }

}
