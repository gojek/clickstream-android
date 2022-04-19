package clickstream.config

/**
 * EventProcessorConfiguration holds the config for event processor for QoSO and QoS1.
 *
 * @param realtimeEvents A list of strings specifying realtime event names
 */
public data class CSEventClassification(
    val realtimeEvents: List<String>,
    val instantEvent: List<String>
) {

    public companion object {

        /**
         * Creates the default instance of the config
         */
        public fun default(): CSEventClassification = CSEventClassification(emptyList(), emptyList())
    }
}
