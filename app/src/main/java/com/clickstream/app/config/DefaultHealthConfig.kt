package com.clickstream.app.config

import DefaultCSHealthGateway
import android.content.Context
import android.util.Log
import clickstream.health.intermediate.CSHealthEventLoggerListener
import clickstream.health.intermediate.CSMemoryStatusProvider
import clickstream.health.model.CSHealthEventConfig
import clickstream.health.time.CSHealthTimeStampGenerator
import clickstream.logger.CSLogLevel
import clickstream.logger.CSLogger


fun getHealthGateway(context: Context) = DefaultCSHealthGateway(
    context = context,
    csInfo = csInfo(),
    logger = CSLogger(CSLogLevel.DEBUG),
    timeStampGenerator = timeStampGenerator(),
    csMemoryStatusProvider = memoryStatusProvider(),
    csHealthEventConfig = healthConfig(),
    csHealthEventLoggerListener = healthEventLogger()
)


fun healthEventLogger() = object : CSHealthEventLoggerListener {
    override fun logEvent(eventName: String, healthData: HashMap<String, Any>) {
        Log.d("CS External Logger", "$eventName: $healthData")
    }

}

fun memoryStatusProvider() = object : CSMemoryStatusProvider {
    override fun isLowMemory(): Boolean {
        return false
    }
}

fun timeStampGenerator() = object : CSHealthTimeStampGenerator {
    override fun getTimeStamp(): Long {
        return System.currentTimeMillis()
    }
}

fun healthConfig() =
    CSHealthEventConfig(
        minTrackedVersion = "1.1.0",
        randomUserIdRemainder = (0..9).toList(),
        destination = listOf("CS", "CT"),
        verbosityLevel = "minimum"
    )