package com.kjipo.bluetoothmidi

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BluetoothMidiApp(
    appContainer: AppContainer,
    widthSizeClass: WindowWidthSizeClass
) {

    AppTheme {
        val navController = rememberNavController()
        val navigationActions = remember(navController) {
            NavigationActions(navController)
        }
        val coroutineScope = rememberCoroutineScope()
        val isExpandedScreen = widthSizeClass == WindowWidthSizeClass.Expanded
        val sizeAwareDrawerState = rememberSizeAwareDrawerState(isExpandedScreen)

        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute =
            navBackStackEntry?.destination?.route ?: NavigationDestinations.HOME.name

        ModalNavigationDrawer(drawerContent = {
            NavigationDrawer(
                currentRoute = currentRoute,
                navigateToMidiDevices = navigationActions.navigateToDevices,
                closeDrawer = { coroutineScope.launch { sizeAwareDrawerState.close() } }
            )
        }) {
            Row {
                AppNavGraph(appContainer = appContainer,
                    navController = navController,
                    connectToDevice = navigationActions.navigateToConnectScreen,
                    toggleScan = {
                        runBlocking {
                            appContainer.deviceScanner.toggleScan()
                        }
                    })
            }
        }

    }

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun rememberSizeAwareDrawerState(isExpandedScreen: Boolean): DrawerState {
    val drawerState = rememberDrawerState(DrawerValue.Closed)

    return if (!isExpandedScreen) {
        // If we want to allow showing the drawer, we use a real, remembered drawer
        // state defined above
        drawerState
    } else {
        // If we don't want to allow the drawer to be shown, we provide a drawer state
        // that is locked closed. This is intentionally not remembered, because we
        // don't want to keep track of any changes and always keep it closed
        DrawerState(DrawerValue.Closed)
    }
}

