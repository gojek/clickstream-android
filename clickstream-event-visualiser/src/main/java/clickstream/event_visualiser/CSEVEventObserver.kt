package clickstream.event_visualiser

import clickstream.interceptor.CSEventInterceptor
import clickstream.interceptor.CSInterceptedEvent


/**
 * Observer class that manages callback and emits event updates from [CSEventInterceptor]
 *
 * */
public interface CSEVEventObserver {

    public fun addCallback(callback: (List<CSInterceptedEvent>) -> Unit)

    public fun removeCallback(callback: (List<CSInterceptedEvent>) -> Unit)

    public fun onEventChanged(events: List<CSInterceptedEvent>)
}