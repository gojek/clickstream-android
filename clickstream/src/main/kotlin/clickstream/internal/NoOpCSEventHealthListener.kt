package clickstream.internal

import clickstream.health.model.CSEventHealth
import clickstream.health.CSEventHealthListener

internal class NoOpCSEventHealthListener : CSEventHealthListener {
    override fun onEventCreated(healthEvent: CSEventHealth) {
        /*No Op*/
    }
}
