package clickstream.eventvisualiser

import clickstream.listener.CSEventListener
import clickstream.listener.CSEventModel


/**
 * Observer class that manages callback and emits event updates from [CSEventListener]
 *
 * */
public interface CSEVEventObserver {

    /**
     * Add observer
     *
     * @param callback
     * @receiver
     */
    public fun addObserver(callback: (List<CSEventModel>) -> Unit)

    /**
     * Remove observer
     *
     * @param callback
     * @receiver
     */
    public fun removeObserver(callback: (List<CSEventModel>) -> Unit)

    /**
     * Triggered on every event update.
     *
     * @param events
     */
    public fun onEventChanged(events: List<CSEventModel>)
}