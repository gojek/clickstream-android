package clickstream.event_visualiser

import clickstream.interceptor.CSInterceptedEvent
import java.util.concurrent.CopyOnWriteArrayList

/**
 * An Implementation of [CSEVEventObserver] that is used by [CSEventVisualiserInterceptor] singleton.
 *
 * */
public object CSEventVisualiser : CSEVEventObserver {

    private val observers = CopyOnWriteArrayList<(List<CSInterceptedEvent>) -> Unit>()

    public override fun addCallback(callback: (List<CSInterceptedEvent>) -> Unit) {
        addUniqueCallback(callback)
    }

    public override fun removeCallback(callback: (List<CSInterceptedEvent>) -> Unit) {
        observers.remove(callback)
    }

    override fun onEventChanged(events: List<CSInterceptedEvent>) {
        observers.forEach {
            it(events)
        }
    }

    private fun addUniqueCallback(callback: (List<CSInterceptedEvent>) -> Unit) {
        observers.run {
            if (!contains(callback)) {
                add(callback)
            }
        }
    }

}