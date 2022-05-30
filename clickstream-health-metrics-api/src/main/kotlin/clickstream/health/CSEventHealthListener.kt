package clickstream.health

/**
 * A listener class which provide a callback for eventCreated.
 */
public interface CSEventHealthListener {
    /**
     * [CSEventHealth] hold event meta information.
     */
    public fun onEventCreated(healthEvent: CSEventHealth)
}
