package clickstream.connection

/**
 * A interface that provides onEventConnectionChange.
 */
public interface CSSocketConnectionListener {

    /**
     * Observes the socket connection - Connecting, Connected, Closed, Closing, Failed, MessageReceived.
     */
    public fun onEventChanged(event: CSConnectionEvent)
}
