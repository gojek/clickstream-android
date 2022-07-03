package clickstream.internal.analytics.impl

import clickstream.health.intermediate.CSHealthEventLoggerListener
import clickstream.health.model.CSHealthEvent

internal class NoOpCSHealthEventLogger : CSHealthEventLoggerListener {

    override fun logEvent(eventName: String, healthEvent: CSHealthEvent) {
        /*No Op*/
    }
}