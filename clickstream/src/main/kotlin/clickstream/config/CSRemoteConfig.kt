package clickstream.config

/**
 * A Remote Configuration A/B testing for ClickStream
 */
public interface CSRemoteConfig {
    /**
     * True if we flushed events on the foreground
     */
    public val isForegroundEventFlushEnabled: Boolean

    public val ignoreBatteryLvlOnFlush: Boolean

    public val batchFlushedEvents: Boolean
}

/**
 * A NoOp implementation of Remote Configuration A/B testing for ClickStream.
 */
public class NoOpCSRemoteConfig : CSRemoteConfig {

    override val isForegroundEventFlushEnabled: Boolean
        get() = false

    override val ignoreBatteryLvlOnFlush: Boolean
        get() = false

    override val batchFlushedEvents: Boolean
        get() = false
}