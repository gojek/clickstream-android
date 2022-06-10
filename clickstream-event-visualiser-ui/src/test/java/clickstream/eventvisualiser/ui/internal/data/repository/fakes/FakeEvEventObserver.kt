package clickstream.eventvisualiser.ui.internal.data.repository.fakes

import clickstream.eventvisualiser.CSEVEventObserver
import clickstream.interceptor.CSInterceptedEvent

internal class FakeEvEventObserver : CSEVEventObserver {

    private val observers = mutableListOf<(List<CSInterceptedEvent>) -> Unit>()

    override fun addObserver(callback: (List<CSInterceptedEvent>) -> Unit) {
        addUniqueCallback(callback)
    }

    override fun removeObserver(callback: (List<CSInterceptedEvent>) -> Unit) {
        observers.remove(callback)
    }

    override fun onEventChanged(events: List<CSInterceptedEvent>) {
        observers.forEach {
            it(events)
        }
    }

    private fun addUniqueCallback(callback: (List<CSInterceptedEvent>) -> Unit) {
        observers.run {
            if (!contains(callback)) {
                add(callback)
            }
        }
    }
}