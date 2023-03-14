package clickstream.eventvisualiser.ui.internal.data

import clickstream.eventvisualiser.ui.internal.data.model.CSEvEvent
import clickstream.eventvisualiser.ui.internal.data.model.CSEvState
import clickstream.listener.CSEventModel
import java.io.BufferedReader
import java.io.StringReader
import java.lang.StringBuilder

internal const val MILLISECOND = 1000L

internal fun CSEventModel.Event.toCsEvent(): CSEvEvent {
    return when (this) {
        is CSEventModel.Event.Instant -> CSEvEvent(
            eventName = eventName ?: "Empty event",
            eventId = eventId,
            properties = properties,
            state = CSEvState.ACKNOWLEDGED,
            timeStampInMillis = timeStamp.toMillisecond(),
        )
        is CSEventModel.Event.Scheduled -> CSEvEvent(
            eventName = eventName ?: "Empty event",
            eventId = eventId,
            properties = properties,
            state = CSEvState.SCHEDULED,
            timeStampInMillis = timeStamp.toMillisecond(),
        )
        else -> throw Exception("Cannot convert $this to CSEvent")
    }
}

private fun Long.toMillisecond() = this * MILLISECOND