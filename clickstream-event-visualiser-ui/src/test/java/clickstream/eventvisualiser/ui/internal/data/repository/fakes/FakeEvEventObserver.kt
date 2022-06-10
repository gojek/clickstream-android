package clickstream.eventvisualiser.ui.internal.data.repository.fakes

import clickstream.eventvisualiser.CSEVEventObserver
import clickstream.listener.CSEventModel

internal class FakeEvEventObserver : CSEVEventObserver {

    private val observers = mutableListOf<(List<CSEventModel>) -> Unit>()

    override fun addObserver(callback: (List<CSEventModel>) -> Unit) {
        addUniqueCallback(callback)
    }

    override fun removeObserver(callback: (List<CSEventModel>) -> Unit) {
        observers.remove(callback)
    }

    override fun onEventChanged(events: List<CSEventModel>) {
        observers.forEach {
            it(events)
        }
    }

    private fun addUniqueCallback(callback: (List<CSEventModel>) -> Unit) {
        observers.run {
            if (!contains(callback)) {
                add(callback)
            }
        }
    }
}