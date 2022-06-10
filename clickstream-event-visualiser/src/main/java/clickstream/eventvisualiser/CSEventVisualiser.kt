package clickstream.eventvisualiser

import clickstream.listener.CSEventModel
import java.util.concurrent.CopyOnWriteArrayList

/**
 * An Implementation of [CSEVEventObserver] that is used by [CSEventVisualiserListener] singleton.
 *
 * */
public object CSEventVisualiser : CSEVEventObserver {

    private val observers = CopyOnWriteArrayList<(List<CSEventModel>) -> Unit>()

    public override fun addObserver(callback: (List<CSEventModel>) -> Unit) {
        addUniqueCallback(callback)
    }

    public override fun removeObserver(callback: (List<CSEventModel>) -> Unit) {
        observers.remove(callback)
    }

    override fun onEventChanged(events: List<CSEventModel>) {
        observers.forEach {
            it(events)
        }
    }

    private fun addUniqueCallback(callback: (List<CSEventModel>) -> Unit) {
        observers.run {
            if (!contains(callback)) {
                add(callback)
            }
        }
    }

}