package com.clickstream.app.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import clickstream.ClickStream
import clickstream.event_visualiser.CSEVEventObserver
import clickstream.model.CSEvent
import com.clickstream.app.helper.Dispatcher
import com.clickstream.app.helper.printMessage
import com.clickstream.app.main.MainIntent.ConnectIntent
import com.clickstream.app.main.MainIntent.DisconnectIntent
import com.clickstream.app.main.MainIntent.InputIntent
import com.clickstream.app.main.MainIntent.SendIntent
import com.clickstream.app.main.MainState.InFlight
import clickstream.interceptor.CSInterceptedEvent
import com.gojek.clickstream.common.App
import com.gojek.clickstream.common.Customer
import com.gojek.clickstream.common.CustomerRole
import com.gojek.clickstream.common.EventMeta
import com.gojek.clickstream.products.common.AppType
import com.gojek.clickstream.products.common.DroppedProperties
import com.gojek.clickstream.products.common.DroppedPropertiesBatch
import com.gojek.clickstream.products.common.Product
import com.gojek.clickstream.products.shuffle.ShuffleArticleCard
import com.gojek.clickstream.products.shuffle.ShuffleCardV2
import com.gojek.clickstream.products.shuffle.ShuffleContent
import com.gojek.clickstream.products.shuffle.ShuffleGroupedCard
import com.gojek.clickstream.products.telemetry.Protocol
import com.gojek.clickstream.products.telemetry.PubSubHealth
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

    private val eventCallback: (List<CSInterceptedEvent>) -> Unit = {
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
        val customerProto = DroppedPropertiesBatch.newBuilder().apply {
            appType = AppType.Consumer
            product = Product.GoSend
            addAllDroppedProperties(
                listOf(
                    DroppedProperties.newBuilder()
                        .addProperties("kshitij")
                        .addProperties("kera")
                        .build(),
                    DroppedProperties.newBuilder()
                        .addProperties("amil")
                        .addProperties("gogo")
                        .build()
                )
            )
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
