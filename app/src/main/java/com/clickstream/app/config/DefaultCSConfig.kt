package com.clickstream.app.config

import clickstream.config.CSConfig
import clickstream.config.CSEventProcessorConfig
import clickstream.config.CSEventSchedulerConfig
import clickstream.config.CSNetworkConfig
import clickstream.health.model.CSHealthEventConfig
import com.clickstream.app.helper.load

fun csConfig(
    accountId: AccountId,
    secretKey: SecretKey,
    endpoint: EndPoint,
    stubBearer: StubBearer
): CSConfig {
    val eventClassification =
        CSEventClassificationParser::class.java.load("clickstream_classifier.json")!!

    return CSConfig(
        eventProcessorConfiguration = CSEventProcessorConfig(
            realtimeEvents = eventClassification.realTimeEvents(),
            instantEvent = eventClassification.instantEvents()
        ),
        eventSchedulerConfig = CSEventSchedulerConfig.default(),
        networkConfig = CSNetworkConfig.default(
            CSNetworkModule.create(accountId, secretKey, stubBearer)
        ).copy(endPoint = endpoint.value),
        healthEventConfig = CSHealthEventConfig.default()
    )
}
