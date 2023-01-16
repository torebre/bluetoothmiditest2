package com.kjipo.bluetoothmidi.ui.sessionview

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue


@Composable
fun SessionScreenRoute(sessionViewModel: SessionViewModel) {
    val uiState by sessionViewModel.uiState.collectAsState()

    SessionInformation(uiState)

}


@Composable
fun SessionInformation(uiState: SessionUiState) {
    Text("${uiState.start}")
}