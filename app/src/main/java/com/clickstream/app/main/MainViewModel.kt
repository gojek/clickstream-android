package com.clickstream.app.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import clickstream.CSEvent
import clickstream.ClickStream
import com.clickstream.app.helper.Dispatcher
import com.clickstream.app.helper.printMessage
import com.clickstream.app.main.MainIntent.ConnectIntent
import com.clickstream.app.main.MainIntent.DisconnectIntent
import com.clickstream.app.main.MainIntent.InputIntent
import com.clickstream.app.main.MainIntent.SendIntent
import com.clickstream.app.main.MainState.InFlight
import com.gojek.clickstream.common.EventMeta
import com.gojek.clickstream.products.common.AppType
import com.gojek.clickstream.products.common.DroppedProperties
import com.gojek.clickstream.products.common.DroppedPropertiesBatch
import com.gojek.clickstream.products.common.Product
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
        val customerProto = DroppedPropertiesBatch.newBuilder().apply {
            appType = AppType.Consumer
            product = Product.GoSend
            meta = EventMeta.newBuilder().apply {
                eventGuid = "123"
            }.build()
            eventTimestamp = Timestamp.newBuilder().apply {
                seconds = System.currentTimeMillis() / 1000L
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
