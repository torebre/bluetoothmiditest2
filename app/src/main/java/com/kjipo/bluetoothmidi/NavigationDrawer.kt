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
    modifier: Modifier = Modifier,
    currentRoute: String
) {

    ModalDrawerSheet(modifier) {

        NavigationDrawerItem(
            label = { Text("MIDI devices") },
            selected = currentRoute == NavigationDestinations.DEVICE_LIST.name,
            onClick = { navigateToMidiDevices(); closeDrawer() },
        )
        NavigationDrawerItem(
            label = { Text("Test2") },
            selected = false,
            onClick = { closeDrawer() },
        )

    }


}