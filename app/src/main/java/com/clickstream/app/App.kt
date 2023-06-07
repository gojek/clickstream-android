package com.clickstream.app

import android.app.Application
import android.provider.Settings
import android.util.Base64
import clickstream.ClickStream
import clickstream.config.CSConfiguration
import clickstream.connection.CSConnectionEvent
import clickstream.connection.CSSocketConnectionListener
import clickstream.eventvisualiser.CSEventVisualiserListener
import clickstream.eventvisualiser.ui.CSEventVisualiserUI
import clickstream.logger.CSLogLevel
import com.clickstream.app.config.csConfig
import com.clickstream.app.config.csInfo
import com.clickstream.app.config.getHealthGateway
import com.clickstream.app.helper.printMessage
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.util.*

@OptIn(ExperimentalCoroutinesApi::class)
@HiltAndroidApp
class App : Application() {

    override fun onCreate() {
        super.onCreate()

        ClickStream.initialize(
            configuration = CSConfiguration.Builder(
                context = this,
                config = csConfig(
                    url = BuildConfig.ENDPOINT,
                    deviceId = deviceId(),
                    apiKey = String(
                        Base64.encode(
                            BuildConfig.API_KEY.toByteArray(),
                            Base64.NO_WRAP
                        )
                    )
                ),
                info = csInfo()
            ).applyLogLevel()
                .applyEventListener()
                .applyHealthFactory()
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

    private fun CSConfiguration.Builder.applyHealthFactory(): CSConfiguration.Builder {
        setHealthGateway(getHealthGateway(this@App))
        return this
    }

    private fun deviceId(): String {
        return Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ANDROID_ID
        ) ?: UUID.randomUUID().toString()
    }
}
