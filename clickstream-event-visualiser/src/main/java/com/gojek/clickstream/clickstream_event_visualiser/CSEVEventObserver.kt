package com.gojek.clickstream.clickstream_event_visualiser

import com.clickstream.clickstream.event_visualiser.interceptor.InterceptedEvent

public abstract class CSEVEventObserver {
    public var enable: Boolean = true
    public abstract fun onNewEvent(list: List<InterceptedEvent>)
}