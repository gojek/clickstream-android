package clickstream.event_visualiser

import clickstream.interceptor.EventInterceptor
import clickstream.interceptor.InterceptedEvent


/**
 * An Clickstream [EventInterceptor] that delegate the intercepted event handling to [CSEventVisualiser].
 * Can be applied to [CSConfiguration.Builder.addInterceptor].
 *
 * Use [getInstance] to create and get a singleton instance.
 *
 * @property csEventObserver
 */
public class CSEventVisualiserInterceptor private constructor(
    private val csEventObserver: CSEVEventObserver
) : EventInterceptor {

    public companion object {

        @Volatile
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

    override fun onIntercept(events: List<InterceptedEvent>) {
        csEventObserver.setNewEvent(events)
    }
}