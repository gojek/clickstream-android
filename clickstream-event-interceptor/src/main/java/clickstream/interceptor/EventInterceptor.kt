package clickstream.interceptor

/**
 * Observes and intercepts all events going out from clickstream.
 *
 */
public interface EventInterceptor {
    /**
     *
     * @param events
     */
    public fun onIntercept(events: List<InterceptedEvent>)
}