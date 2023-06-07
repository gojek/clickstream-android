package clickstream.config.timestamp

/**
 * A default implementation of [CSEventGeneratedTimestampListener].
 */
public class DefaultCSEventGeneratedTimestampListener : CSEventGeneratedTimestampListener {
    override fun now(): Long {
        return System.currentTimeMillis()
    }
}