package clickstream.health.time

/**
 * An interface to generate time that will being use by internal events.
 *
 * in most cases the implementation detail would use NTP client instead of device system clock.
 */
public interface CSEventGeneratedTimestampListener {
    /**
     * return NTP time
     */
    public fun now(): Long
}