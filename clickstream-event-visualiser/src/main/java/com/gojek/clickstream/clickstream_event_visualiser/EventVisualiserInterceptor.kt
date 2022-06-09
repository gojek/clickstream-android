package com.gojek.clickstream.clickstream_event_visualiser

import clickstream.interceptor.EventInterceptor
import clickstream.interceptor.InterceptedEvent

public class EventVisualiserInterceptor(private val eventRepository: EVEventRepository) :
    EventInterceptor {

    override fun onIntercept(interceptedEventBatch: InterceptedEvent) {
        eventRepository.setNewEvent(interceptedEventBatch)
    }

    public companion object {
        public fun getDefault(): EventVisualiserInterceptor =
            EventVisualiserInterceptor(EVEventRepository)
    }
}