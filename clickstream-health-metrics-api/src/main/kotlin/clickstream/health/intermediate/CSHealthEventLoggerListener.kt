package clickstream.health.intermediate

import clickstream.health.model.CSHealthEvent

/**
 * ClickStreamTracker can be implemented by host app to provide observer for ClickStream events
 */
public interface CSHealthEventLoggerListener {

    /**
     * Method called to notify observer about ClickStream events
     * @param eventName
     * @param healthEvent
     */
    public fun logEvent(eventName: String, healthEvent: CSHealthEvent)
}
