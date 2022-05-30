package clickstream.internal.analytics.impl

import clickstream.health.intermediate.CSHealthEventLoggerListener

internal class NoOpCSHealthEventLogger : CSHealthEventLoggerListener {

    override fun logEvent(eventName: String, eventData: HashMap<String, Any>) {
        /*No Op*/
    }
}