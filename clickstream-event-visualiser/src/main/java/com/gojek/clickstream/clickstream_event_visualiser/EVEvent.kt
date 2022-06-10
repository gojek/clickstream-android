package com.gojek.clickstream.clickstream_event_visualiser

import org.json.JSONObject

public abstract class EVEvent(
    public open val eventId: String,
    public open val eventName: String?,
    public open val productName: String,
    public open val timeStamp: Long
) {
    public class Instant(
        override val eventId: String,
        override val eventName: String?,
        override val productName: String,
        override val timeStamp: Long,
        public val properties: JSONObject
    ) : EVEvent(eventId, eventName, productName, timeStamp)

    public class Scheduled(
        override val eventId: String,
        override val eventName: String?,
        override val productName: String,
        override val timeStamp: Long,
        public val properties: JSONObject
    ) : EVEvent(eventId, eventName, productName, timeStamp)

    public class Dispatched(
        override val eventId: String,
        override val eventName: String?,
        override val productName: String,
        override val timeStamp: Long,
    ) : EVEvent(eventId, eventName, productName, timeStamp)

    public class Acknowledged(
        override val eventId: String,
        override val eventName: String?,
        override val productName: String,
        override val timeStamp: Long,
    ) : EVEvent(eventId, eventName, productName, timeStamp)

}