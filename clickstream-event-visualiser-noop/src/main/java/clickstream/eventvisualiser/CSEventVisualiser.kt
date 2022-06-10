package clickstream.eventvisualiser

import clickstream.listener.CSEventModel

/**
 * An Implementation of [CSEVEventObserver] that is used by [CSEventVisualiserListener] singleton.
 *
 * */
public object CSEventVisualiser : CSEVEventObserver {

    public override fun addObserver(observer: (List<CSEventModel>) -> Unit) {
        /*NoOp*/
    }

    public override fun removeObserver(observer: (List<CSEventModel>) -> Unit) {
        /*NoOp*/
    }

    override fun onEventChanged(events: List<CSEventModel>) {
        /*NoOp*/
    }
}