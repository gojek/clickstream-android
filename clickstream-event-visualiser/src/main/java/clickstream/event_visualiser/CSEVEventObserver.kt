package clickstream.event_visualiser

import clickstream.interceptor.InterceptedEvent

public abstract class CSEVEventObserver {
    public var enable: Boolean = true
    public abstract fun onNewEvent(list: List<InterceptedEvent>)
}