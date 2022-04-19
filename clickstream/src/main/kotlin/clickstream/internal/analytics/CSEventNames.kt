package clickstream.internal.analytics

internal enum class CSEventNames(val value: String) {
    ClickStreamFailedInit("ClickStream Failed Init"),
    ClickStreamEventReceived("Clickstream Event Received"),
    ClickStreamEventObjectCreated("Clickstream Event Object Created"),
    ClickStreamEventCached("Clickstream Event Cached"),
    ClickStreamEventBatchCreated("Clickstream Event Batch Created"),
    ClickStreamEventBatchTriggerFailed("Clickstream Event Batch Trigger Failed"),
    ClickStreamEventBatchErrorResponse("Clickstream Event Batch Error response"),
    ClickStreamBatchSent("ClickStream Batch Sent"),
    ClickStreamBatchWriteFailed("ClickStream Write to Socket Failed"),
    ClickStreamConnectionFailed("ClickStream Connection Failed"),
    ClickStreamEventBatchAck("Clickstream Event Batch Success Ack"),
    ClickStreamEventBatchTimeout("Clickstream Event Batch Timeout"),
    ClickStreamEventBatchLatency("ClickStream Event Batch Latency"),
    ClickStreamEventWaitTime("ClickStream Event Wait Time"),
    ClickStreamEventBatchWaitTime("ClickStream Event Batch Wait Time"),
    ClickStreamBatchSize("ClickStream Batch Size"),
    ClickStreamFlushOnBackground("ClickStream Flush On Background"),
    ClickStreamBackgroundServiceCompleted("ClickStream Background Service Completed"),
    ClickStreamInvalidMessage("Clickstream Event Unsupported String Received")
}

internal object CSErrorReasons {
    const val PARSING_EXCEPTION = "parsing_exception"
    const val LOW_BATTERY = "low_battery"
    const val NETWORK_UNAVAILABLE = "network_unavailable"
    const val SOCKET_NOT_OPEN = "socket_not_open"
    const val UNKNOWN = "unknown"
    const val USER_UNAUTHORIZED = "401 Unauthorized"
    const val SOCKET_TIMEOUT = "socket_timeout"
    const val MAX_USER_LIMIT_REACHED = "max_user_limit_reached"
    const val MAX_CONNECTION_LIMIT_REACHED = "max_connection_limit_reached"
}

internal object EventTypes {
    const val INSTANT = "instant"
    const val AGGREGATE = "aggregate"
    const val BUCKET = "bucket"
}