package com.clickstream.app.main

import com.gojek.clickstream.clickstream_event_visualiser.CSEVEvent

sealed class MainState {

    object InFlight : MainState()
    class CSEventState(val list: List<CSEVEvent>) : MainState()

    data class Content(
        val isNameInputNull: Boolean
    )
}