package clickstream.health.intermediate

/**
 * ClickStreamTracker can be implemented by host app to provide observer for ClickStream events
 */
public interface CSHealthEventLoggerListener {

    /**
     * Method called to notify observer about ClickStream events
     * @param eventName
     * @param eventData
     */
    public fun logEvent(eventName: String, eventData: HashMap<String, Any>)
}
