package clickstream.analytics.event.impl

import clickstream.analytics.event.CSEventHealth
import clickstream.analytics.event.CSEventHealthListener

internal class NoOpCSEventHealthListener : CSEventHealthListener {
    override fun onEventCreated(healthEvent: CSEventHealth) {
        /*No Op*/
    }
}
