package clickstream.eventvisualiser

import clickstream.listener.CSEventListener
import clickstream.listener.CSEventModel

/**
 * An Clickstream [CSEventListener] that delegate the event handling to [CSEventVisualiser].
 * Can be applied to [CSConfiguration.Builder.addEventListener].
 *
 * Use [getInstance] to create and get a singleton instance.
 *
 * @property csEventObserver
 */
public class CSEventVisualiserListener private constructor(
    private val csEventObserver: CSEVEventObserver
) : CSEventListener {

    override fun onCall(events: List<CSEventModel>) {
        csEventObserver.onEventChanged(events)
    }

    public companion object {

        @Volatile
        private lateinit var INSTANCE: CSEventVisualiserListener
        private var lock = Any()

        public fun getInstance(): CSEventVisualiserListener {
            if (!Companion::INSTANCE.isInitialized) {
                synchronized(lock) {
                    if (!Companion::INSTANCE.isInitialized) {
                        INSTANCE = CSEventVisualiserListener(CSEventVisualiser)
                    }
                }
            }
            return INSTANCE
        }
    }

}