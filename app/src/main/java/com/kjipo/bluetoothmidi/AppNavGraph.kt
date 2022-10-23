package com.kjipo.bluetoothmidi

import android.app.Activity
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
import com.kjipo.bluetoothmidi.bluetooth.BluetoothConnect
import com.kjipo.bluetoothmidi.bluetooth.BluetoothPairing
import com.kjipo.bluetoothmidi.devicelist.DeviceListViewModel
import com.kjipo.bluetoothmidi.devicelist.MidiDevicesUiState
import com.kjipo.bluetoothmidi.ui.midirecord.MidiDeviceList

enum class NavigationDestinations {
    HOME,
    DEVICE_LIST,
    CONNECT,
    SCAN2
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

    val navigateToScan: () -> Unit = {
        navController.navigate(NavigationDestinations.SCAN2.name) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    val navigateToConnectScreen: (String) -> Unit = { deviceAddress ->
        navController.navigate("${NavigationDestinations.CONNECT.name}/$deviceAddress") {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

}

@Composable
fun AppNavGraph(
    appContainer: AppContainer,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = NavigationDestinations.HOME.name,
    connectToDevice: (String) -> Unit,
    activity: Activity
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(NavigationDestinations.HOME.name) {
            HomeRoute()
        }
        composable(NavigationDestinations.DEVICE_LIST.name) {
            val deviceListModel: DeviceListViewModel =
                viewModel(factory = DeviceListViewModel.provideFactory(appContainer.deviceScanner))
            MidiDeviceListRoute(deviceListModel, connectToDevice)
        }
        composable(NavigationDestinations.CONNECT.name) {
            // TODO
//            ConnectRoute()
        }
        composable(NavigationDestinations.SCAN2.name) {
//            BluetoothConnect(bluetoothPairing = appContainer.bluetoothPairing)
            BluetoothConnect(bluetoothPairing = BluetoothPairing(activity))
        }
    }

}


@Composable
fun MidiDeviceListRoute(
    deviceListViewModel: DeviceListViewModel,
    connect: (String) -> Unit
) {
    val uiState by deviceListViewModel.uiState.collectAsState()

    MidiDeviceListRoute(
        uiState,
        toggleScan = {
            deviceListViewModel.toggleScan() },
            connect = connect
            )
        }


@Composable
fun MidiDeviceListRoute(
    uiState: MidiDevicesUiState, toggleScan: () -> Unit,
    connect: (String) -> Unit
) {
    MidiDeviceList(toggleScan, uiState.isScanning, connect, uiState.foundDevices)
}


@Composable
fun ConnectRoute() {
    // TODO

}