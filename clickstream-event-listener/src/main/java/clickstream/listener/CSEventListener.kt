package clickstream.listener

/**
 * Interface that observes all events going out from clickstream.
 *
 * Use [CSConfiguration.Builder.addEventListener] in clickstream  to add your implementation of
 * [CSEventListener].
 *
 * The [onCall] method is invoked on the dispatcher thread that was
 * set using [CSConfiguration.Builder.setDispatcher]
 *
 */
public interface CSEventListener {
    /**
     * Invoked on any event update.
     *
     * @param events
     */
    public fun onCall(events: List<CSEventModel>)
}