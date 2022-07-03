package clickstream.fake

import clickstream.listener.CSEventListener
import clickstream.listener.CSEventModel

internal class FakeCSEventListener : CSEventListener {

    var isCalled = false

    override fun onCall(events: List<CSEventModel>) {
        isCalled = true
    }
}