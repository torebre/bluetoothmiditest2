package com.kjipo.bluetoothmidi.ui.homescreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn


class HomeScreenModel : ViewModel() {

    private val viewModelState = MutableStateFlow(HomeScreenModelUiState(""))

    val uiState =
        viewModelState.stateIn(viewModelScope, SharingStarted.Eagerly, viewModelState.value)


    companion object {

        fun provideFactory(): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return HomeScreenModel() as T
                }
            }
    }

}


data class HomeScreenModelUiState(val previouslyConnectedDevice: String)