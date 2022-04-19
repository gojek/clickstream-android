package com.clickstream.app

import android.app.Application
import clickstream.ClickStream
import clickstream.config.CSConfiguration
import com.clickstream.app.config.CSConfig
import com.clickstream.app.config.CSInfo
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
@HiltAndroidApp
class App : Application() {

    override fun onCreate() {
        super.onCreate()

        ClickStream.initialize(
            configuration = CSConfiguration.Builder(
                context = this,
                info = CSInfo(),
                config = CSConfig()
            ).build()
        )
    }
}
