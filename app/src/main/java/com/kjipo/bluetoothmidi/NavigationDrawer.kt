package com.kjipo.bluetoothmidi

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationDrawer(
    navigateToMidiDevices: () -> Unit,
    closeDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {

    ModalDrawerSheet(modifier) {

        NavigationDrawerItem(
            label = { Text("MIDI devices") },
            onClick = { navigateToMidiDevices(); closeDrawer() },
            selected = true
        )
        NavigationDrawerItem(
            label = { Text("Test2") },
            onClick = { closeDrawer() },
            selected = false
        )

    }


}