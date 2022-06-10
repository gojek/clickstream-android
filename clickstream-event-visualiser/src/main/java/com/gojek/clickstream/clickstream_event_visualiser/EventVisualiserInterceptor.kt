package com.gojek.clickstream.clickstream_event_visualiser

import clickstream.config.CSConfiguration
import clickstream.interceptor.EventInterceptor
import clickstream.interceptor.InterceptedEvent


/**
 * An Clickstream [EventInterceptor] that delegate the intercepted event handling to [EVEventRepository].
 * Can be applied to [CSConfiguration.Builder.addInterceptor].
 *
 * Use [getInstance] to create and get a default instance.
 *
 * @property eventRepository
 */
public class EventVisualiserInterceptor(private val eventRepository: EVEventRepository) :
    EventInterceptor {

    override fun onIntercept(event: InterceptedEvent) {
        eventRepository.setNewEvent(event)
    }

    public companion object {
        /**
         * Factory method to create an instance.
         *
         * @return [EventVisualiserInterceptor]
         */
        public fun getInstance(): EventVisualiserInterceptor =
            EventVisualiserInterceptor(EVEventRepository)
    }
}