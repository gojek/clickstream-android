package com.clickstream.app.main

import clickstream.event_visualiser.CSEVEventObserver
import clickstream.event_visualiser.CSEventVisualiser
import com.clickstream.app.helper.Dispatcher
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

@InstallIn(SingletonComponent::class)
@Module
internal object MainModule {

    @Provides
    fun dispatcher(): Dispatcher {
        return object : Dispatcher {
            override val io: CoroutineDispatcher
                get() = Dispatchers.IO
            override val default: CoroutineDispatcher
                get() = Dispatchers.Default
            override val main: CoroutineDispatcher
                get() = Dispatchers.Main
        }
    }

    @Provides
    fun getEventVisualiser(): CSEVEventObserver = CSEventVisualiser
}