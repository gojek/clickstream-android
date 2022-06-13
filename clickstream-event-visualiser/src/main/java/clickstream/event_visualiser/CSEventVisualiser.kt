package clickstream.event_visualiser

import clickstream.interceptor.InterceptedEvent
import java.util.concurrent.atomic.AtomicReference

/**
 * An Implementation of [CSEVEventObserver] that is used by [CSEventVisualiserInterceptor] singleton.
 *
 * */
public object CSEventVisualiser : CSEVEventObserver {

    private val observers = AtomicReference(mutableListOf<(List<InterceptedEvent>) -> Unit>())

    public override fun addCallback(callback: (List<InterceptedEvent>) -> Unit) {
        addUniqueCallback(callback)
    }

    public override fun removeCallback(callback: (List<InterceptedEvent>) -> Unit) {
        observers.get().remove(callback)
    }

    override fun setNewEvent(events: List<InterceptedEvent>) {
        observers.get().forEach {
            it(events)
        }
    }

    private fun addUniqueCallback(callback: (List<InterceptedEvent>) -> Unit) {
        observers.get().run {
            if (!contains(callback)) {
                add(callback)
            }
        }
    }

}