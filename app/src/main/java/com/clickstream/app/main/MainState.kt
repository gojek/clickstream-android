package com.clickstream.app.main

sealed class MainState {

    object InFlight : MainState()

    data class Content(
        val isNameInputNull: Boolean
    )
}