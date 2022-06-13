package com.clickstream.app.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import clickstream.ClickStream
import clickstream.event_visualiser.CSEVEventObserver
import clickstream.event_visualiser.CSEventVisualiser
import clickstream.model.CSEvent
import com.clickstream.app.helper.Dispatcher
import com.clickstream.app.helper.printMessage
import com.clickstream.app.main.MainIntent.ConnectIntent
import com.clickstream.app.main.MainIntent.DisconnectIntent
import com.clickstream.app.main.MainIntent.InputIntent
import com.clickstream.app.main.MainIntent.SendIntent
import com.clickstream.app.main.MainState.InFlight
import clickstream.interceptor.InterceptedEvent
import com.gojek.clickstream.common.Customer
import com.gojek.clickstream.common.CustomerRole
import com.google.protobuf.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    val dispatcher: Dispatcher,
    private val csEv: CSEVEventObserver
) : ViewModel() {

    private val eventCallback: (List<InterceptedEvent>) -> Unit = {
        viewModelScope.launch { _states.emit(MainState.InterceptedEventState(it)) }

    }

    init {
        startObservingEventChange()
    }

    private val _states = MutableStateFlow<MainState>(InFlight)
    val states: StateFlow<MainState>
        get() = _states

    fun processIntents(flows: Flow<MainIntent>) {
        viewModelScope.launch {
            flows.collect { intent ->
                when (intent) {
                    is ConnectIntent -> {
                        startObservingEventChange()
                    }
                    is DisconnectIntent -> {
                        stopObservingEventChange()
                    }
                    is SendIntent -> {
                        sendMockCSEvent()
                    }
                    is InputIntent -> printMessage { "age: ${intent.age}, name: ${intent.name}" }
                }
            }
        }
    }

    private fun startObservingEventChange() {
        csEv.addCallback(eventCallback)
    }

    private fun stopObservingEventChange() {
        csEv.removeCallback(eventCallback)
    }

    private fun sendMockCSEvent() {
        val customerProto = Customer.newBuilder().apply {
            currentCountry = "India"
            email = "kshitij.sharma@gojek.com"
            identity = 78
            signedUpCountry = "India"
            role = CustomerRole.CUSTOMER_ROLE_CASHIER
        }.build()
        ClickStream.getInstance().trackEvent(
            CSEvent(
                UUID.randomUUID().toString(),
                Timestamp.getDefaultInstance(),
                customerProto
            ), false
        )
    }
}
