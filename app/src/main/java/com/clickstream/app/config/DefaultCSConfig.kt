package com.clickstream.app.config

import clickstream.config.CSConfig
import clickstream.config.CSEventClassification
import clickstream.config.CSEventSchedulerConfig
import clickstream.config.CSNetworkConfig
import com.clickstream.app.helper.load
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.google.gson.internal.bind.DateTypeAdapter
import java.util.Date
import okhttp3.OkHttpClient

fun CSConfig(): CSConfig {
    val eventClassification = CSEventClassificationParser::class.java.load("clickstream_classifier.json")!!

    return CSConfig(
        eventProcessorConfiguration = CSEventClassification(
            realtimeEvents = eventClassification.realTimeEvents(),
            instantEvent = eventClassification.instantEvents()
        ),
        eventSchedulerConfig = CSEventSchedulerConfig.default(),
        networkConfig = CSNetworkConfig.default(OkHttpClient()).copy(endPoint = "YOUR ENDPOINT")
    )
}
