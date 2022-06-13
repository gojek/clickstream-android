package com.gojek.clickstream.clickstream_event_visualiser

import clickstream.extension.eventName
import clickstream.extension.toJson
import clickstream.extension.protoName
import clickstream.interceptor.InterceptedEvent

internal fun InterceptedEvent.toEVEvent(): List<CSEVEvent> =
    when (this) {
        is InterceptedEvent.Instant -> listOf(
            CSEVEvent.Instant(
                eventId = event.eventGuid,
                eventName = messageLite.eventName() ?: "",
                productName = messageLite.protoName(),
                timeStamp = event.eventTimeStamp,
                properties = messageLite.toJson()
            )
        )
        is InterceptedEvent.Scheduled -> listOf(
            CSEVEvent.Scheduled(
                eventId = event.eventGuid,
                eventName = messageLite.eventName() ?: "",
                productName = messageLite.protoName(),
                timeStamp = event.eventTimeStamp,
                properties = messageLite.toJson()
            )
        )
        is InterceptedEvent.Dispatched -> events.map {
            val rawEvent = it.event()
            CSEVEvent.Dispatched(
                eventId = it.eventGuid,
                eventName = rawEvent.eventName() ?: "",
                productName = rawEvent.protoName(),
                timeStamp = it.eventTimeStamp,
            )
        }
        is InterceptedEvent.Acknowledged -> events.map {
            val rawEvent = it.event()
            CSEVEvent.Acknowledged(
                eventId = it.eventGuid,
                eventName = rawEvent.eventName() ?: "",
                productName = rawEvent.protoName(),
                timeStamp = it.eventTimeStamp,
            )
        }
        else -> events.map {
            val rawEvent = it.event()
            CSEVEvent.Acknowledged(
                eventId = it.eventGuid,
                eventName = rawEvent.eventName() ?: "",
                productName = rawEvent.protoName(),
                timeStamp = it.eventTimeStamp,
            )
        }
    }