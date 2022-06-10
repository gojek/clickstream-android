package clickstream.eventvisualiser

import clickstream.listener.CSEventListener
import clickstream.listener.CSEventModel


/**
 * An Clickstream [CSEventListener] that delegate the intercepted event handling to [CSEventVisualiser].
 * Can be applied to [CSConfiguration.Builder.addEventListener].
 *
 * Use [getInstance] to create and get a singleton instance.
 *
 * @property csEventObserver
 */
public class CSEventVisualiserListener private constructor() : CSEventListener {

    public companion object {
        @Volatile
        private lateinit var INSTANCE: CSEventVisualiserListener
        private var lock = Any()

        public fun getInstance(): CSEventVisualiserListener {
            if (!::INSTANCE.isInitialized) {
                synchronized(lock) {
                    if (!::INSTANCE.isInitialized) {
                        INSTANCE = CSEventVisualiserListener()
                    }
                }
            }
            return INSTANCE
        }
    }

    override fun onCall(events: List<CSEventModel>) {
        /*NoOp*/
    }
}