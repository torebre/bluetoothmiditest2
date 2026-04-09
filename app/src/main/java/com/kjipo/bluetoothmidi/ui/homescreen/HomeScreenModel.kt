package com.kjipo.bluetoothmidi.ui.homescreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kjipo.bluetoothmidi.midi.MidiHandler
import com.kjipo.bluetoothmidi.session.Session
import com.kjipo.bluetoothmidi.session.SessionDatabase
import com.kjipo.bluetoothmidi.ui.sessionlist.MidiSessionData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.CountDownLatch
import kotlin.concurrent.timerTask


class HomeScreenModel(
    private val midiHandler: MidiHandler,
    private val sessionDatabase: SessionDatabase
) :
    ViewModel() {

    private var lastConnectedDeviceAddress: String? = null

    private val viewModelState = MutableStateFlow(HomeScreenModelUiState())

    val uiState =
        viewModelState.stateIn(viewModelScope, SharingStarted.Eagerly, viewModelState.value)

    init {
        Timber.tag("Bluetooth").i("Last connected device address: $lastConnectedDeviceAddress")
    }


    fun loadData() {
        viewModelScope.launch(Dispatchers.IO) {
            sessionDatabase.sessionDao().getMostRecentSession()?.let { mostRecentSession ->
                viewModelState.update {
                    it.copy(
                        previousSession = transformToMidiSessionData(
                            mostRecentSession
                        )
                    )
                }
            }
        }
    }

    fun attemptConnectToPreviouslyConnectedDevice(lastConnectedDeviceAddress: String) {
        lastConnectedDeviceAddress.let { address ->
            viewModelState.update { it.copy(state = HomeState.CONNECTING) }

            val latch = CountDownLatch(1)
            Timer("midi_device_connecting").schedule(timerTask {
                if (latch.count == 1L) {
                    latch.countDown()
                    viewModelState.update { it.copy(state = HomeState.FAILED_TO_CONNECT) }
                }
            }, 10000)

            viewModelScope.launch {
                Timber.tag("Bluetooth").i("Opening device with address: $address")
                midiHandler.openDevice(address) {
                    latch.countDown()
                    viewModelState.update {
                        it.copy(
                            state = HomeState.CONNECTED,
                        )
                    }
                }
            }
        }
    }

    private fun transformToMidiSessionData(session: Session): MidiSessionData {
        return MidiSessionData(
            session.uid,
            LocalDateTime.ofInstant(session.start, ZoneId.systemDefault()),
            LocalDateTime.ofInstant(session.sessionEnd, ZoneId.systemDefault())
        )
    }


    companion object {

        fun provideFactory(
            midiHandler: MidiHandler,
            sessionDatabase: SessionDatabase
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return HomeScreenModel(midiHandler, sessionDatabase) as T
                }
            }
    }

}

data class HomeScreenModelUiState(
    val state: HomeState = HomeState.STARTING,
    val previousSession: MidiSessionData? = null
)


enum class HomeState {
    STARTING,
    CONNECTING,
    CONNECTED,
    FAILED_TO_CONNECT
}