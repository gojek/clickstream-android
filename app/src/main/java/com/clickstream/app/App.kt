package com.clickstream.app

import android.app.Application
import clickstream.ClickStream
import clickstream.config.CSConfiguration
import clickstream.connection.CSConnectionEvent
import clickstream.connection.CSSocketConnectionListener
import clickstream.eventvisualiser.CSEventVisualiserListener
import clickstream.eventvisualiser.ui.CSEventVisualiserUI
import clickstream.health.constant.CSTrackedVia
import clickstream.lifecycle.impl.DefaultCSAppLifeCycleObserver
import clickstream.logger.CSLogLevel
import clickstream.logger.CSLogger
import com.clickstream.app.config.AccountId
import com.clickstream.app.config.EndPoint
import com.clickstream.app.config.SecretKey
import com.clickstream.app.config.StubBearer
import com.clickstream.app.config.csConfig
import com.clickstream.app.config.csInfo
import com.clickstream.app.helper.printMessage
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
@HiltAndroidApp
class App : Application() {

    override fun onCreate() {
        super.onCreate()

        val csLogger = CSLogger(CSLogLevel.DEBUG)

        ClickStream.initialize(
            configuration = CSConfiguration.Builder(
                context = this,
                info = csInfo(),
                config = csConfig(
                    AccountId(BuildConfig.ACCOUNT_ID),
                    SecretKey(BuildConfig.SECRET_KEY),
                    EndPoint(BuildConfig.ENDPOINT),
                    StubBearer(BuildConfig.STUB_BEARER),
                    CSTrackedVia.Both
                ),
                appLifeCycle = DefaultCSAppLifeCycleObserver(csLogger)
            )
            .applyLogLevel()
            .applyEventListener()
            .applySocketConnectionListener()
            .build()
        )

        CSEventVisualiserUI.initialise(this)
    }

    private fun CSConfiguration.Builder.applyLogLevel(): CSConfiguration.Builder {
        setLogLevel(CSLogLevel.DEBUG)
        return this
    }

    private fun CSConfiguration.Builder.applyEventListener(): CSConfiguration.Builder {
        addEventListener(CSEventVisualiserListener.getInstance())
        return this
    }

    private fun CSConfiguration.Builder.applySocketConnectionListener(): CSConfiguration.Builder {
        setSocketConnectionListener(object : CSSocketConnectionListener {
            override fun onEventChanged(event: CSConnectionEvent) {
                when (event) {
                    is CSConnectionEvent.OnConnectionClosed -> printMessage { "OnConnectionClosed due to ${event.shutdownReason}" }
                    is CSConnectionEvent.OnConnectionClosing -> printMessage { "OnConnectionClosing due to ${event.shutdownReason}" }
                    is CSConnectionEvent.OnConnectionConnected -> printMessage { "Connected" }
                    is CSConnectionEvent.OnConnectionConnecting -> printMessage { "Connecting" }
                    is CSConnectionEvent.OnConnectionFailed -> printMessage { "OnConnectionFailed due to ${event.throwable.message ?: "broken"}" }
                    is CSConnectionEvent.OnMessageReceived -> printMessage { "OnMessageReceived : ${event.message}" }
                }
            }
        })
        return this
    }
}
