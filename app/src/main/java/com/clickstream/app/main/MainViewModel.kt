package com.clickstream.app.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clickstream.app.helper.Dispatcher
import com.clickstream.app.main.MainIntent.ConnectIntent
import com.clickstream.app.main.MainIntent.DisconnectIntent
import com.clickstream.app.main.MainIntent.InputIntent
import com.clickstream.app.main.MainIntent.SendIntent
import com.clickstream.app.main.MainState.InFlight
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val dispatcher: Dispatcher
) : ViewModel() {

    private val _states = MutableStateFlow<MainState>(InFlight)
    val states: StateFlow<MainState>
        get() = _states

    fun processIntents(flows: Flow<MainIntent>) {
        viewModelScope.launch {
            flows.collect { intent ->
                when (intent) {
                    is ConnectIntent -> {}
                    is DisconnectIntent -> {}
                    is SendIntent -> {}
                    is InputIntent -> print { "age: ${intent.age}, name: ${intent.name}" }
                }
            }
        }
    }
}
