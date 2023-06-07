package clickstream.internal.health

import clickstream.health.intermediate.CSHealthEventLoggerListener
import clickstream.health.model.CSHealthEvent

internal class NoOpCSHealthEventLoggerListener : CSHealthEventLoggerListener {

    override fun logEvent(eventName: String, healthEvent: CSHealthEvent) {
        /*NoOp*/
    }
}