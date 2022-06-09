package com.gojek.clickstream.clickstream_event_visualiser

import clickstream.interceptor.InterceptedEvent

public object EVEventRepository {

    public var onNewEvent: (List<EVEvent>) -> Unit = {}

    private var listenToEvent = true

    public fun startListeningToEvent() {
        listenToEvent = true
    }

    public fun stopListeningToEvent() {
        listenToEvent = false
    }

    internal fun setNewEvent(interceptedEvent: InterceptedEvent) {
        if (listenToEvent) {
            onNewEvent.invoke(interceptedEvent.toEVEvent())
        }
    }
}