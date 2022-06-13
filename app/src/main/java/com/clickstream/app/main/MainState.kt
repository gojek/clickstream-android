package com.clickstream.app.main

import com.clickstream.clickstream.event_visualiser.interceptor.InterceptedEvent

sealed class MainState {

    object InFlight : MainState()
    class InterceptedEventState(val list: List<InterceptedEvent>) : MainState()

    data class Content(
        val isNameInputNull: Boolean
    )
}