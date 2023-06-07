package com.clickstream.app.config

import android.content.Context
import clickstream.health.DefaultCSHealthGateway
import clickstream.health.constant.CSTrackedVia
import clickstream.health.intermediate.CSEventHealthListener
import clickstream.health.intermediate.CSHealthEventLoggerListener
import clickstream.health.model.CSEventHealth
import clickstream.health.model.CSHealthEvent
import clickstream.health.model.CSHealthEventConfig
import clickstream.health.time.CSHealthTimeStampGenerator
import clickstream.lifecycle.impl.DefaultCSAppLifeCycleObserver
import clickstream.logger.CSLogLevel
import clickstream.logger.CSLogger


fun getHealthGateway(context: Context) =
    DefaultCSHealthGateway.factory(
        appVersion = "1.0.0",
        sessionId = "abc",
        context = context,
        healthEventConfig = CSHealthEventConfig(
            "1.0.0",
            listOf(), CSTrackedVia.Both
        ),
        csInfo = csInfo(),
        logger = CSLogger(CSLogLevel.DEBUG),
        healthEventLogger = healthEventLogger(),
        eventHealthListener = healthEventListener(),
        timeStampGenerator = timeStampGenerator(),
        appLifeCycle = DefaultCSAppLifeCycleObserver(CSLogger(CSLogLevel.DEBUG))
    )


fun healthEventLogger() = object : CSHealthEventLoggerListener {
    override fun logEvent(eventName: String, healthEvent: CSHealthEvent) {

    }
}

fun healthEventListener() = object : CSEventHealthListener {
    override fun onEventCreated(healthEvent: CSEventHealth) {

    }
}

fun timeStampGenerator() = object : CSHealthTimeStampGenerator {
    override fun getTimeStamp(): Long {
        return System.currentTimeMillis()
    }

}