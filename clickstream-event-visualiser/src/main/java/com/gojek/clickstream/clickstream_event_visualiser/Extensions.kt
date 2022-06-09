package com.gojek.clickstream.clickstream_event_visualiser

import clickstream.extension.eventName
import clickstream.extension.toJson
import clickstream.extension.protoName
import clickstream.interceptor.InterceptedEvent

internal fun InterceptedEvent.toEVEvent(): List<EVEvent> =
    when (this) {
        is InterceptedEvent.Instant -> listOf(
            EVEvent.Instant(
                eventId = event.eventGuid,
                eventName = messageLite.eventName() ?: "",
                productName = messageLite.protoName(),
                timeStamp = event.eventTimeStamp,
                properties = messageLite.toJson()
            )
        )
        is InterceptedEvent.Scheduled -> listOf(
            EVEvent.Scheduled(
                eventId = event.eventGuid,
                eventName = messageLite.eventName() ?: "",
                productName = messageLite.protoName(),
                timeStamp = event.eventTimeStamp,
                properties = messageLite.toJson()
            )
        )
        is InterceptedEvent.Dispatched -> events.map {
            val rawEvent = it.event()
            EVEvent.Dispatched(
                eventId = it.eventGuid,
                eventName = rawEvent.eventName() ?: "",
                productName = rawEvent.protoName(),
                timeStamp = it.eventTimeStamp,
            )
        }
        is InterceptedEvent.Registered -> events.map {
            val rawEvent = it.event()
            EVEvent.Registered(
                eventId = it.eventGuid,
                eventName = rawEvent.eventName() ?: "",
                productName = rawEvent.protoName(),
                timeStamp = it.eventTimeStamp,
            )
        }
    }