package clickstream.fake

import clickstream.lifecycle.CSAppLifeCycle
import clickstream.lifecycle.CSAppLifeCycleObserver

internal class FakeCSAppLifeCycle : CSAppLifeCycle {

    val observers = mutableListOf<CSAppLifeCycleObserver>()

    override fun addObserver(observer: CSAppLifeCycleObserver) {
        observers.add(observer)
    }
}