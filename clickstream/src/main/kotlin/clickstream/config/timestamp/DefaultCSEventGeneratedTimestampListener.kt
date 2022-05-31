package clickstream.config.timestamp

import clickstream.health.time.CSEventGeneratedTimestampListener

/**
 * A default implementation of [CSEventGeneratedTimestampListener].
 */
public class DefaultCSEventGeneratedTimestampListener : CSEventGeneratedTimestampListener {
    override fun now(): Long {
        return System.currentTimeMillis()
    }
}