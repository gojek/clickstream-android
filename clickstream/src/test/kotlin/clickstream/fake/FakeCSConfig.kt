package clickstream.fake

import clickstream.config.CSConfig
import clickstream.config.CSEventProcessorConfig
import clickstream.config.CSEventSchedulerConfig
import clickstream.config.CSNetworkConfig
import clickstream.health.constant.CSTrackedVia
import clickstream.health.model.CSHealthEventConfig

internal fun createCSConfig(): CSConfig {
    return CSConfig(
        eventProcessorConfiguration = CSEventProcessorConfig(
            realtimeEvents = emptyList(),
            instantEvent = listOf("AdCardEvent")
        ),
        eventSchedulerConfig = CSEventSchedulerConfig.default(),
        networkConfig = CSNetworkConfig.default(createOkHttpClient()).copy(endPoint = ""),
        healthEventConfig = CSHealthEventConfig.default(CSTrackedVia.Both)
    )
}