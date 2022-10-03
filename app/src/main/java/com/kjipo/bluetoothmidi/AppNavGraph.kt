package com.kjipo.bluetoothmidi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kjipo.bluetoothmidi.devicelist.DeviceListViewModel
import com.kjipo.bluetoothmidi.devicelist.MidiDevicesUiState
import com.kjipo.bluetoothmidi.ui.midirecord.MidiDeviceList

enum class NavigationDestinations {
    DEVICE_LIST,
//    RECORD_DATA,
//    VIEW_DATA
}

class NavigationActions(navController: NavHostController) {
    val navigateToDevices: () -> Unit = {
        navController.navigate(NavigationDestinations.DEVICE_LIST.name) {
            // Pop up to the start destination of the graph to
            // avoid building up a large stack of destinations
            // on the back stack as users select items
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            // Avoid multiple copies of the same destination when
            // reselecting the same item
            launchSingleTop = true
            // Restore state when reselecting a previously selected item
            restoreState = true
        }
    }
}

@Composable
fun AppNavGraph(
    appContainer: AppContainer,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = NavigationDestinations.DEVICE_LIST.name
) {

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(NavigationDestinations.DEVICE_LIST.name) {
            val deviceListModel: DeviceListViewModel =
                viewModel(factory = DeviceListViewModel.provideFactory(appContainer.deviceScanner))
            MidiDeviceListRoute(deviceListModel)
        }
    }

}


@Composable
fun MidiDeviceListRoute(deviceListViewModel: DeviceListViewModel) {
    val uiState by deviceListViewModel.uiState.collectAsState()

    MidiDeviceListRoute(uiState)

}

@Composable
fun MidiDeviceListRoute(uiState: MidiDevicesUiState) {

    MidiDeviceList(foundDevices = uiState.foundDevices)

}