package com.kjipo.bluetoothmidi

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.accompanist.permissions.*
import com.kjipo.bluetoothmidi.ui.mididevicelist.BluetoothConnect
import com.kjipo.bluetoothmidi.ui.mididevicelist.BluetoothHandler
import com.kjipo.bluetoothmidi.connect.MidiSessionViewModel
import com.kjipo.bluetoothmidi.ui.mididevicelist.DeviceListViewModel
import com.kjipo.bluetoothmidi.midi.EarTrainer
import com.kjipo.bluetoothmidi.midi.MidiHandler
import com.kjipo.bluetoothmidi.ui.midiplay.PlayViewModel
import com.kjipo.bluetoothmidi.session.MidiSessionRepository
import com.kjipo.bluetoothmidi.session.SessionDatabase
import com.kjipo.bluetoothmidi.ui.homescreen.HomeScreenModel
import com.kjipo.bluetoothmidi.ui.mididevicelist.MidiDeviceListRoute
import com.kjipo.bluetoothmidi.ui.midiplay.PlayMidi
import com.kjipo.bluetoothmidi.ui.sessionlist.MidiSessionUi
import com.kjipo.bluetoothmidi.ui.sessionlist.MidiSessionUiInput
import com.kjipo.bluetoothmidi.ui.sessionview.SessionScreenRoute
import com.kjipo.bluetoothmidi.ui.sessionview.SessionViewModel
import timber.log.Timber
import java.io.File

enum class NavigationDestinations {
    HOME,
    DEVICE_LIST,
    MIDI_RECORD,
    MIDI_PLAY,

    //    SCAN2,
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

    // TODO
//    val navigateToScan: () -> Unit = {
//        navController.navigate(NavigationDestinations.SCAN2.name) {
//            popUpTo(navController.graph.findStartDestination().id) {
//                saveState = true
//            }
//            launchSingleTop = true
//            restoreState = true
//        }
//    }

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
    sessionDatabase: SessionDatabase,
    earTrainer: EarTrainer
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(NavigationDestinations.HOME.name) {
            Timber.tag("NavGraph").i("Local view: ${LocalView.current}")
            // TODO Just trying to set viewModelStoreOwner to the NavBackStackEntry to see what happens
            val homeScreenViewModel: HomeScreenModel = viewModel(
                viewModelStoreOwner = it,
                factory = HomeScreenModel.provideFactory(
                    midiHandler,
                    sessionDatabase
                )
            )

            HomeRoute(homeScreenViewModel)
        }

        composable(NavigationDestinations.DEVICE_LIST.name) {
            val deviceListModel: DeviceListViewModel =
                viewModel(
                    factory = DeviceListViewModel.provideFactory(
                        deviceScanner, midiHandler
                    )
                )

            MidiDeviceListRoute(
                deviceListModel, { address, sharedPreferences ->
                    deviceListModel.connectToDevice(address, sharedPreferences)
                })
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
        // TODO
//        composable(NavigationDestinations.SCAN2.name) {
//            BluetoothConnect(bluetoothPairing = BluetoothHandler(mainActivity))
//        }
        composable(
            NavigationDestinations.MIDI_SESSION_LIST.name
        ) {
            val sessionViewModel: MidiSessionListViewModel =
                viewModel(factory = MidiSessionListViewModel.provideFactory(midiSessionRepository))
            MidiSessionUi(midiSessionUiInputData = MidiSessionUiInput(
                sessionViewModel.uiState,
                navigateToSessionInformation
            ) { sessionIds ->
//                val directoryToExportTo = File(mainActivity.applicationContext.cacheDir) //, "export_files")
//                val fileToExportTo = File(directoryToExportTo, "export_file_temp.zip")
                val fileToExportTo =
                    File(mainActivity.applicationContext.cacheDir, "export_file_temp.zip")

                // TODO The view model should not have a reference to the activity, something which it gets indirectly here
                val shareCallback = {
                    val fileUri = FileProvider.getUriForFile(
                        mainActivity.applicationContext,
                        "com.kjipo.bluetoothmidi",
                        fileToExportTo
                    )

                    val shareIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_STREAM, fileUri)
                        type = "application/zip"
                    }
                    mainActivity.startActivity(shareIntent)
                }

                sessionViewModel.exportData(sessionIds, fileToExportTo, shareCallback)
            })
        }

        composable(NavigationDestinations.MIDI_PLAY.name) {
            val midiPlayViewModel: PlayViewModel =
                viewModel(factory = PlayViewModel.provideFactory(midiHandler, earTrainer))
            PlayMidi(midiPlayViewModel)
        }

        composable(
            "${NavigationDestinations.SESSION_VIEW.name}/{sessionId}",
            arguments = listOf(navArgument("sessionId") { type = NavType.StringType })
        ) { navBackStackEntry ->
            // TODO Why does it not work with getLong?
            val sessionId = navBackStackEntry.arguments?.getString("sessionId")!!

            val sessionViewModel: SessionViewModel =
                viewModel(
                    factory = SessionViewModel.provideFactory(
                        sessionId.toLong(),
                        sessionDatabase.sessionDao()
                    )
                )
            SessionScreenRoute(sessionViewModel)
        }

    }

}

