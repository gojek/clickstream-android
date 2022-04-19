package com.clickstream.app.config

import clickstream.config.CSConfig
import clickstream.config.CSEventClassification
import clickstream.config.CSEventSchedulerConfig
import clickstream.config.CSNetworkConfig
import okhttp3.OkHttpClient

fun CSConfig() = CSConfig(
    eventProcessorConfiguration = CSEventClassification(emptyList(), emptyList()),
    eventSchedulerConfig = CSEventSchedulerConfig.default(),
    networkConfig = CSNetworkConfig.default(OkHttpClient())
)