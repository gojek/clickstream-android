package clickstream.fake

import clickstream.health.intermediate.CSHealthEventLoggerListener
import clickstream.health.model.CSHealthEvent

internal class FakeCSHealthEventLoggerListener : CSHealthEventLoggerListener {

    val record = mutableMapOf<String, CSHealthEvent>()

    override fun logEvent(eventName: String, healthEvent: CSHealthEvent) {
        record[eventName] = healthEvent
    }
}