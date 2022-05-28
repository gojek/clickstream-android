package clickstream.internal.analytics

/**
 * ClickStreamTracker can be implemented by host app to provide observer for ClickStream events
 */
public interface CSHealthEventLogger {

    /**
     * Method called to notify observer about ClickStream events
     * @param eventName
     * @param eventData
     */
    public fun logEvent(eventName: String, eventData: HashMap<String, Any>)
}
