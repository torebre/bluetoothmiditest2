package com.kjipo.bluetoothmidi

import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier


@Composable
fun NavigationDrawer(
    navigateToMidiDevices: () -> Unit,
    navigateToMidiRecord: () -> Unit,
    navigateToMidiPlay: () -> Unit,
    closeDrawer: () -> Unit,
    modifier: Modifier = Modifier,
    currentRoute: String,
//    navigateToScan: () -> Unit,
    navigateToSessionList: () -> Unit
) {

    ModalDrawerSheet(modifier) {
        NavigationDrawerItem(
            label = { Text("MIDI devices") },
            selected = currentRoute == NavigationDestinations.DEVICE_LIST.name,
            onClick = { navigateToMidiDevices(); closeDrawer() },
        )
//        NavigationDrawerItem(
//            label = { Text("Scan") },
//            selected = currentRoute == NavigationDestinations.SCAN2.name,
//            onClick = { navigateToScan(); closeDrawer() },
//        )
        NavigationDrawerItem(
            label = { Text("MIDI record") },
            selected = currentRoute == NavigationDestinations.MIDI_RECORD.name,
            onClick = { navigateToMidiRecord(); closeDrawer() },
        )
        NavigationDrawerItem(
            label = { Text("MIDI play") },
            selected = currentRoute == NavigationDestinations.MIDI_PLAY.name,
            onClick = { navigateToMidiPlay(); closeDrawer() },
        )
        NavigationDrawerItem(
            label = { Text("Sessions") },
            selected = currentRoute == NavigationDestinations.MIDI_SESSION_LIST.name,
            onClick = { navigateToSessionList(); closeDrawer() },
        )
    }

}