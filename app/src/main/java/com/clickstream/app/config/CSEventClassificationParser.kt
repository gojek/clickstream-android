package com.clickstream.app.config

import com.google.gson.annotations.SerializedName

data class CSEventClassificationParser(
    @SerializedName("eventTypes")
    val eventTypes: List<EventType> = emptyList()
) {

    fun realTimeEvents(): List<String> =
        eventTypes
            .filter { it.identifier == "realTime" }
            .flatMap { it.eventNames }

    fun instantEvents(): List<String> =
        eventTypes
            .filter { it.identifier == "instant" }
            .flatMap { it.eventNames }

    data class EventType(
        @SerializedName("eventNames") val eventNames: List<String>,
        @SerializedName("identifier") val identifier: String, // realTime, standard, instant
    )
}