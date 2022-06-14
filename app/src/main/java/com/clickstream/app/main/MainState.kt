package com.clickstream.app.main

import clickstream.interceptor.CSInterceptedEvent

sealed class MainState {

    object InFlight : MainState()
    class InterceptedEventState(val list: List<CSInterceptedEvent>) : MainState()

    data class Content(
        val isNameInputNull: Boolean
    )
}