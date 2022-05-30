package clickstream.internal.analytics.impl

import clickstream.health.CSHealthEventLogger

internal class NoOpCSHealthEventLogger : CSHealthEventLogger {

    override fun logEvent(eventName: String, eventData: HashMap<String, Any>) {
        /*No Op*/
    }
}