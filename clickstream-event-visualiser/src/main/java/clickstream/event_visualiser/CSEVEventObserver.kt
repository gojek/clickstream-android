package clickstream.event_visualiser

import clickstream.interceptor.EventInterceptor
import clickstream.interceptor.InterceptedEvent


/**
 * Observer class that manages callback and emits event updates from [EventInterceptor]
 *
 * */
public interface CSEVEventObserver {

    public fun addCallback(callback: (List<InterceptedEvent>) -> Unit)

    public fun removeCallback(callback: (List<InterceptedEvent>) -> Unit)

    public fun setNewEvent(events: List<InterceptedEvent>)
}