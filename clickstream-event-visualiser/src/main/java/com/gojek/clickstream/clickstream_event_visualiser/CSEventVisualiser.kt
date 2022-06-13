package com.gojek.clickstream.clickstream_event_visualiser

import com.clickstream.clickstream.event_visualiser.interceptor.InterceptedEvent
import java.util.concurrent.atomic.AtomicReference

public object CSEventVisualiser {

    private val observers = AtomicReference(mutableListOf<CSEVEventObserver>())

    public fun addObserver(observer: CSEVEventObserver) {
        observers.get().add(observer)
    }

    public fun removeObserver(observer: CSEVEventObserver) {
        observers.get().remove(observer)
    }

    internal fun setNewEvent(events: List<InterceptedEvent>) {
        observers.get().forEach {
            if (it.enable) {
                it.onNewEvent(events)
            }
        }
    }
}