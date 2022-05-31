package clickstream.config

import kotlin.properties.Delegates

/**
 * EventProcessorConfiguration holds the config for event processor.
 *
 * @param realtimeEvents A list of strings specifying realtime event names
 */
public data class CSEventProcessorConfig(
    val realtimeEvents: List<String>,
    val instantEvent: List<String>
) {
    public var instantEventExpiredInSeconds: Int by Delegates.notNull()

    public constructor(
        realTimeEvents: List<String>,
        instantEventsAndExpired: Pair<List<String>, Int>
    ) : this(realTimeEvents, instantEventsAndExpired.first) {
        this.instantEventExpiredInSeconds = instantEventsAndExpired.second
    }

    public companion object {

        /**
         * Creates the default instance of the config
         */
        public fun default(): CSEventProcessorConfig = CSEventProcessorConfig(emptyList(), emptyList())
    }
}
