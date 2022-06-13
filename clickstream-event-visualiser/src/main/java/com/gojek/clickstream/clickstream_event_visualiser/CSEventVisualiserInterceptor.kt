package com.gojek.clickstream.clickstream_event_visualiser

import clickstream.config.CSConfiguration
import clickstream.interceptor.EventInterceptor
import clickstream.interceptor.InterceptedEvent
import net.jcip.annotations.GuardedBy


/**
 * An Clickstream [EventInterceptor] that delegate the intercepted event handling to [CSEVEventObserver].
 * Can be applied to [CSConfiguration.Builder.addInterceptor].
 *
 * Use [getInstance] to create and get a default instance.
 *
 * @property eventRepository
 */
public class CSEventVisualiserInterceptor private constructor(private val csEventVisualiser: CSEventVisualiser) :
    EventInterceptor {

    override fun onIntercept(event: InterceptedEvent) {
        csEventVisualiser.setNewEvent(event)
    }

    public companion object {

        @Volatile
        @GuardedBy("lock")
        private lateinit var csEventInterceptor: CSEventVisualiserInterceptor
        private var lock = Any()

        public fun getInstance(): CSEventVisualiserInterceptor {
            if (!::csEventInterceptor.isInitialized) {
                synchronized(lock) {
                    if (!::csEventInterceptor.isInitialized) {
                        csEventInterceptor = CSEventVisualiserInterceptor(CSEventVisualiser)
                    }
                }
            }
            return csEventInterceptor
        }
    }
}