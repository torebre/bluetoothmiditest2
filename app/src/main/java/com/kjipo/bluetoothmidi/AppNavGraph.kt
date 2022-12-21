package com.kjipo.bluetoothmidi

import android.Manifest
import android.app.Activity
import android.os.Build
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.accompanist.permissions.*
import com.kjipo.bluetoothmidi.bluetooth.BluetoothConnect
import com.kjipo.bluetoothmidi.bluetooth.BluetoothPairing
import com.kjipo.bluetoothmidi.connect.ConnectViewModel
import com.kjipo.bluetoothmidi.devicelist.DeviceListViewModel
import com.kjipo.bluetoothmidi.devicelist.MidiDevicesUiState
import com.kjipo.bluetoothmidi.session.MidiSessionRepository
import com.kjipo.bluetoothmidi.ui.midirecord.MidiDeviceList
import com.kjipo.bluetoothmidi.ui.midirecord.MidiDeviceListInput
import com.kjipo.bluetoothmidi.ui.sessionlist.MidiSessionUi

enum class NavigationDestinations {
    HOME,
    DEVICE_LIST,
    CONNECT,
    SCAN2,
    MIDI_SESSION_LIST
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

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun AppNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = NavigationDestinations.HOME.name,
    connectToDevice: (String) -> Unit,
    activity: Activity,
    deviceScanner: DeviceScanner,
    midiSessionRepository: MidiSessionRepository
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
            val bluetoothPermissionState = if (Build.VERSION.SDK_INT <= 30) {
                rememberPermissionState(Manifest.permission.BLUETOOTH)
            } else {
                rememberPermissionState(Manifest.permission.BLUETOOTH_CONNECT)
            }
            val deviceListModel: DeviceListViewModel =
                viewModel(factory = DeviceListViewModel.provideFactory(deviceScanner))
            MidiDeviceListRoute(deviceListModel, connectToDevice, bluetoothPermissionState)
        }
        composable(
            "${NavigationDestinations.CONNECT.name}/{address}",
            arguments = listOf(navArgument("address") { type = NavType.StringType })
        ) { backStackEntry ->
            val address = backStackEntry.arguments?.getString("address")!!
            val connectViewModel: ConnectViewModel = viewModel(
                factory = ConnectViewModel.provideFactory(
                    activity.applicationContext,
                    address,
                    midiSessionRepository
                )
            )
            ConnectRoute(connectViewModel)
        }
        composable(NavigationDestinations.SCAN2.name) {
            BluetoothConnect(bluetoothPairing = BluetoothPairing(activity))
        }
        composable(
            NavigationDestinations.MIDI_SESSION_LIST.name
        ) {
            val sessionViewModel: MidiSessionViewModel = viewModel(factory = MidiSessionViewModel.provideFactory(midiSessionRepository))
            MidiSessionUi(midiSessionUiInputData = sessionViewModel.uiState)
        }
    }

}


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MidiDeviceListRoute(
    deviceListViewModel: DeviceListViewModel,
    connect: (String) -> Unit,
    bluetoothPermissionState: PermissionState
) {
    val uiState by deviceListViewModel.uiState.collectAsState()

    MidiDeviceListRoute(
        uiState,
        toggleScan = {
            deviceListViewModel.toggleScan()
        },
        connect = connect,
        bluetoothPermissionState
    )
}


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MidiDeviceListRoute(
    uiState: MidiDevicesUiState, toggleScan: () -> Unit,
    connect: (String) -> Unit,
    bluetoothPermissionState: PermissionState
) {
    val selectedDevice = remember {
        mutableStateOf("address")
    }

    Column {
        when (bluetoothPermissionState.status) {
            is PermissionStatus.Granted -> {
                Text("Bluetooth connect granted")
            }
            is PermissionStatus.Denied -> {
                Row {
                    if (bluetoothPermissionState.status.shouldShowRationale) {
                        Text("Need Bluetooth connect permission")
                    } else {
                        Text("Bluetooth connect permission has been denied")
                    }
                }

                Row {
                    Button(onClick = {
                        bluetoothPermissionState.launchPermissionRequest()
                    }) {
                        Text("Request permission")
                    }

                }
            }
        }

        Row {
            MidiDeviceList(MidiDeviceListInput(toggleScan, uiState.isScanning, connect, uiState.foundDevices, selectedDevice))
        }
    }

}
