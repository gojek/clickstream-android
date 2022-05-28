package clickstream.config

/**
 * A Remote Configuration A/B testing for ClickStream
 */
public interface CSRemoteConfig {
    /**
     * True if we flushed events on the foreground
     */
    public val isForegroundEventFlushEnabled: Boolean

    /**
     * Disabled this value would avoid health metrics to be tracked via clickstream.
     */
    public val isHealthMetricsEnabled: Boolean
}

/**
 * A NoOp implementation of Remote Configuration A/B testing for ClickStream.
 */
public class NoOpCSRemoteConfig(
    override val isForegroundEventFlushEnabled: Boolean = false,
    override val isHealthMetricsEnabled: Boolean = false
) : CSRemoteConfig