package com.clickstream.app.config

import clickstream.config.CSConfig
import clickstream.config.CSEventProcessorConfig
import clickstream.config.CSEventSchedulerConfig
import clickstream.config.CSNetworkConfig
import clickstream.health.model.CSHealthEventConfig
import com.clickstream.app.helper.load

fun csConfig(
    url: String,
    deviceId: String,
    apiKey: String,
): CSConfig {
    val eventClassification =
        CSEventClassificationParser::class.java.load("clickstream_classifier.json")!!

    return CSConfig(
        eventProcessorConfiguration = CSEventProcessorConfig(
            realtimeEvents = eventClassification.realTimeEvents(),
            instantEvent = eventClassification.instantEvents()
        ),
        eventSchedulerConfig = CSEventSchedulerConfig.default().copy(backgroundTaskEnabled = true),
        networkConfig = CSNetworkConfig.default(
            url = url, mapOf(
                "Authorization" to "Basic $apiKey",
                "X-UniqueId" to deviceId
            )
        ),
    )
}
