package com.clickstream.app.main

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
}