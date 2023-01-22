package com.kjipo.bluetoothmidi

import android.Manifest
import android.app.Activity
import android.content.Context
import android.os.Build
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
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
import com.kjipo.bluetoothmidi.connect.MidiSessionViewModel
import com.kjipo.bluetoothmidi.devicelist.DeviceListViewModel
import com.kjipo.bluetoothmidi.devicelist.MidiDevicesUiState
import com.kjipo.bluetoothmidi.midi.MidiHandler
import com.kjipo.bluetoothmidi.midi.PlayViewModel
import com.kjipo.bluetoothmidi.session.MidiSessionRepository
import com.kjipo.bluetoothmidi.session.SessionDatabase
import com.kjipo.bluetoothmidi.ui.homescreen.HomeScreenModel
import com.kjipo.bluetoothmidi.ui.midiplay.PlayMidi
import com.kjipo.bluetoothmidi.ui.mididevicelist.MidiDeviceList
import com.kjipo.bluetoothmidi.ui.mididevicelist.MidiDeviceListInput
import com.kjipo.bluetoothmidi.ui.sessionlist.MidiSessionUi
import com.kjipo.bluetoothmidi.ui.sessionlist.MidiSessionUiInput
import com.kjipo.bluetoothmidi.ui.sessionview.SessionScreenRoute
import com.kjipo.bluetoothmidi.ui.sessionview.SessionViewModel
import timber.log.Timber

enum class NavigationDestinations {
    HOME,
    DEVICE_LIST,
    MIDI_RECORD,
    MIDI_PLAY,
    SCAN2,
    MIDI_SESSION_LIST,
    SESSION_VIEW
}

class NavigationActions(navController: NavHostController) {
    val navigateToHome: () -> Unit = {
        navController.navigate(NavigationDestinations.HOME.name) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = false
        }
    }

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

    val navigateToMidiRecord: () -> Unit = {
        navController.navigate(NavigationDestinations.MIDI_RECORD.name) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    val navigateToMidiPlay: () -> Unit = {
        navController.navigate(NavigationDestinations.MIDI_PLAY.name) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    val navigateToSessionList: () -> Unit = {
        navController.navigate(NavigationDestinations.MIDI_SESSION_LIST.name) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    val navigateToSessionView: (Long) -> Unit = { sessionId ->
        navController.navigate("${NavigationDestinations.SESSION_VIEW.name}/$sessionId") {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = false
            }
            launchSingleTop = true
            restoreState = false
        }
    }

}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun AppNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = NavigationDestinations.HOME.name,
    navigateToHome: () -> Unit,
    navigateToSessionInformation: (Long) -> Unit,
    mainActivity: Activity,
    deviceScanner: DeviceScanner,
    midiHandler: MidiHandler,
    midiSessionRepository: MidiSessionRepository,
    sessionDatabase: SessionDatabase
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(NavigationDestinations.HOME.name) {
            Timber.tag("NavGraph").i("Local view: ${LocalView.current}")
            // TODO Just trying to set viewModelStoreOwner to the NavBackStackEntry to see what happens
            val homeScreenViewModel: HomeScreenModel = viewModel(viewModelStoreOwner = it, factory = HomeScreenModel.provideFactory(midiHandler, mainActivity.getPreferences(Context.MODE_PRIVATE)))

            HomeRoute(homeScreenViewModel)
        }
        composable(NavigationDestinations.DEVICE_LIST.name) {
            val bluetoothPermissionState = if (Build.VERSION.SDK_INT <= 30) {
                rememberPermissionState(Manifest.permission.BLUETOOTH)
            } else {
                rememberPermissionState(Manifest.permission.BLUETOOTH_CONNECT)
            }
            val deviceListModel: DeviceListViewModel =
                viewModel(factory = DeviceListViewModel.provideFactory(deviceScanner, midiHandler, mainActivity.getPreferences(
                    Context.MODE_PRIVATE)))
            MidiDeviceListRoute(
                deviceListModel, {
                    deviceListModel.connectToDevice(it)
                },
                bluetoothPermissionState
            )
        }
        composable(
            NavigationDestinations.MIDI_RECORD.name
        ) {
            val midiSessionViewModel: MidiSessionViewModel = viewModel(
                factory = MidiSessionViewModel.provideFactory(
                    mainActivity.applicationContext,
                    midiHandler,
                    midiSessionRepository,
                    navigateToHome
                )
            )
            MidiSessionRoute(midiSessionViewModel) {
                navigateToHome()
            }
        }
        composable(NavigationDestinations.SCAN2.name) {
            BluetoothConnect(bluetoothPairing = BluetoothPairing(mainActivity))
        }
        composable(
            NavigationDestinations.MIDI_SESSION_LIST.name
        ) {
            val sessionViewModel: MidiSessionListViewModel =
                viewModel(factory = MidiSessionListViewModel.provideFactory(midiSessionRepository))
            MidiSessionUi(midiSessionUiInputData = MidiSessionUiInput(sessionViewModel.uiState, navigateToSessionInformation))
        }

        composable(NavigationDestinations.MIDI_PLAY.name) {
            val midiPlayViewModel: PlayViewModel =
                viewModel(factory = PlayViewModel.provideFactory(midiHandler))
            PlayMidi(onClickPlay = { midiPlayViewModel.play() })
        }

        composable("${NavigationDestinations.SESSION_VIEW.name}/{sessionId}", arguments = listOf(navArgument("sessionId") { type = NavType.StringType})) { navBackStackEntry ->
            // TODO Why does it not work with getLong?
            val sessionId = navBackStackEntry.arguments?.getString("sessionId")!!

            val sessionViewModel: SessionViewModel =
                viewModel(factory = SessionViewModel.provideFactory(sessionId.toLong(), sessionDatabase.sessionDao()))
            SessionScreenRoute(sessionViewModel)
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
    uiState: MidiDevicesUiState,
    toggleScan: () -> Unit,
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
            MidiDeviceList(
                MidiDeviceListInput(
                    toggleScan,
                    uiState.isScanning,
                    connect,
                    uiState.foundDevices,
                    selectedDevice,
                    uiState.connectedDeviceAddress
                )
            )
        }
    }

}
