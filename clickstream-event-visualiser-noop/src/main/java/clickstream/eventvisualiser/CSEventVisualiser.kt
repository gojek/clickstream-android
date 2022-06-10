package clickstream.eventvisualiser

import clickstream.interceptor.CSInterceptedEvent

/**
 * An Implementation of [CSEVEventObserver] that is used by [CSEventVisualiserInterceptor] singleton.
 *
 * */
public object CSEventVisualiser : CSEVEventObserver {

    public override fun addObserver(observer: (List<CSInterceptedEvent>) -> Unit) {
        /*NoOp*/
    }

    public override fun removeObserver(observer: (List<CSInterceptedEvent>) -> Unit) {
        /*NoOp*/
    }

    override fun onEventChanged(events: List<CSInterceptedEvent>) {
        /*NoOp*/
    }
}