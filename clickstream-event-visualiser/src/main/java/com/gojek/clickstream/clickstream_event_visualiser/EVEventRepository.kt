package com.gojek.clickstream.clickstream_event_visualiser

import clickstream.interceptor.InterceptedEvent

/**
 * Singleton class that handles intercepted event from [EventVisualiserInterceptor].
 * Set [onNewEvent] to receive the updated events.
 *
 * */
public object EVEventRepository {

    /**
     * Callback to receive updated events.
     *
     * */
    public var onNewEvent: (List<EVEvent>) -> Unit = {}

    private var listenToEvent = true

    /**
     * Enables event updates.
     *
     * */
    public fun startListeningToEvent() {
        listenToEvent = true
    }

    /**
     * Disables event updates.
     *
     * */
    public fun stopListeningToEvent() {
        listenToEvent = false
    }

    internal fun setNewEvent(interceptedEvent: InterceptedEvent) {
        if (listenToEvent) {
            onNewEvent.invoke(interceptedEvent.toEVEvent())
        }
    }
}