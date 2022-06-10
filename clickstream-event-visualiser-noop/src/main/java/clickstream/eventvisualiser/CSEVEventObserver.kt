package clickstream.eventvisualiser

import clickstream.interceptor.CSEventInterceptor
import clickstream.interceptor.CSInterceptedEvent


/**
 * Observer class that manages callback and emits event updates from [CSEventInterceptor]
 *
 * */
public interface CSEVEventObserver {

    /**
     * Add observer
     *
     * @param callback
     * @receiver
     */
    public fun addObserver(callback: (List<CSInterceptedEvent>) -> Unit)

    /**
     * Remove observer
     *
     * @param callback
     * @receiver
     */
    public fun removeObserver(callback: (List<CSInterceptedEvent>) -> Unit)

    /**
     * Triggered on every event update.
     *
     * @param events
     */
    public fun onEventChanged(events: List<CSInterceptedEvent>)
}