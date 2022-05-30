package clickstream.config

/**
 * A Remote Configuration A/B testing for ClickStream
 */
public interface CSRemoteConfig {
    /**
     * True if we flushed events on the foreground
     */
    public val isForegroundEventFlushEnabled: Boolean
}

/**
 * A NoOp implementation of Remote Configuration A/B testing for ClickStream.
 */
public class NoOpCSRemoteConfig(
    override val isForegroundEventFlushEnabled: Boolean = false
) : CSRemoteConfig