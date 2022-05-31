package clickstream.fake

import clickstream.lifecycle.CSAppLifeCycle
import clickstream.lifecycle.CSAppLifeCycleObserver

internal class FakeCSAppLifecycle : CSAppLifeCycle {

    val observers = mutableListOf<CSAppLifeCycleObserver>()

    override fun addObserver(observer: CSAppLifeCycleObserver) {
        observers.add(observer)
    }
}