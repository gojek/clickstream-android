package com.clickstream.app.main

import clickstream.interceptor.InterceptedEvent

sealed class MainState {

    object InFlight : MainState()
    class InterceptedEventState(val list: List<InterceptedEvent>) : MainState()

    data class Content(
        val isNameInputNull: Boolean
    )
}