package com.clickstream.app.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import clickstream.ClickStream
import clickstream.model.CSEvent
import com.clickstream.app.helper.Dispatcher
import com.clickstream.app.helper.printMessage
import com.clickstream.app.main.MainIntent.ConnectIntent
import com.clickstream.app.main.MainIntent.DisconnectIntent
import com.clickstream.app.main.MainIntent.InputIntent
import com.clickstream.app.main.MainIntent.SendIntent
import com.clickstream.app.main.MainState.InFlight
import com.clickstream.app.proto.App
import com.clickstream.app.proto.Device
import com.clickstream.app.proto.User
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
class MainViewModel @Inject constructor(val dispatcher: Dispatcher) : ViewModel() {

    private val _states = MutableStateFlow<MainState>(InFlight)
    val states: StateFlow<MainState>
        get() = _states

    fun processIntents(flows: Flow<MainIntent>) {
        viewModelScope.launch {
            flows.collect { intent ->
                when (intent) {
                    is ConnectIntent -> {}
                    is DisconnectIntent -> {}
                    is SendIntent -> {
                        sendMockCSEvent()
                    }
                    is InputIntent -> printMessage { "age: ${intent.age}, name: ${intent.name}" }
                }
            }
        }
    }

    private fun sendMockCSEvent() {
        val customerProto = User.newBuilder().apply {
            guid = "Some Guid"
            name = "John Doe"
            age = 35
            gender = "male"
            phoneNumber = 1234567890
            email = "john.doe@example.com"
            app = App.newBuilder().apply {
                version = "0.0.1"
                packageName = "com.clickstream"
            }.build()
            device = Device.newBuilder().apply {
                operatingSystem = "android"
                operatingSystemVersion = "29"
                deviceMake = "Samsung"
                deviceModel = "SM2028"
            }.build()
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
