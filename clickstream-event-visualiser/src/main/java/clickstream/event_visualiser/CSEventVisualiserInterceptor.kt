package clickstream.event_visualiser

import com.clickstream.clickstream.event_visualiser.interceptor.EventInterceptor
import com.clickstream.clickstream.event_visualiser.interceptor.InterceptedEvent


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

    public companion object {

        @Volatile
        private lateinit var csEventInterceptor: CSEventVisualiserInterceptor
        private var lock = Any()

        public fun getInstance(): CSEventVisualiserInterceptor {
            if (!Companion::csEventInterceptor.isInitialized) {
                synchronized(lock) {
                    if (!Companion::csEventInterceptor.isInitialized) {
                        csEventInterceptor = CSEventVisualiserInterceptor(CSEventVisualiser)
                    }
                }
            }
            return csEventInterceptor
        }
    }

    override fun onIntercept(events: List<InterceptedEvent>) {
        CSEventVisualiser.setNewEvent(events)
    }
}