package clickstream.config

/**
 * The config which holds the configuration for processor, scheduler & network manager
 *
 * @property eventProcessorConfiguration defines the config for event processor
 * @property eventSchedulerConfig defines the config for event scheduler
 * @property networkConfig defines the config for network manager
 */
public data class CSConfig(
    val eventProcessorConfiguration: CSEventClassification,
    val eventSchedulerConfig: CSEventSchedulerConfig,
    val networkConfig: CSNetworkConfig
)