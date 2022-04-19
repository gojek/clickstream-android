package com.clickstream.app.helper

import kotlinx.coroutines.CoroutineDispatcher

interface Dispatcher {
    val io: CoroutineDispatcher
    val default: CoroutineDispatcher
    val main: CoroutineDispatcher
}