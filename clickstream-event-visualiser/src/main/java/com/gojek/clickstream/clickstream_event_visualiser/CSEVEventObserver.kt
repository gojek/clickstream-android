package com.gojek.clickstream.clickstream_event_visualiser

public abstract class CSEVEventObserver {
    public var enable: Boolean = true
    public abstract fun onNewEvent(list: List<CSEVEvent>)
}