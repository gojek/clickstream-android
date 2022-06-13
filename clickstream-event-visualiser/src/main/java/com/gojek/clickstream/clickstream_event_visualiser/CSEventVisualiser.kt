package com.gojek.clickstream.clickstream_event_visualiser

import clickstream.interceptor.InterceptedEvent
import java.util.concurrent.atomic.AtomicReference

public object CSEventVisualiser {

    private val observers = AtomicReference(mutableListOf<CSEVEventObserver>())

    public fun addObserver(observer: CSEVEventObserver) {
        observers.get().add(observer)
    }

    public fun removeObserver(observer: CSEVEventObserver) {
        observers.get().remove(observer)
    }

    internal fun setNewEvent(interceptedEvent: InterceptedEvent) {
        observers.get().forEach {
            if (it.enable) {
                it.onNewEvent(interceptedEvent.toEVEvent())
            }
        }
    }
}