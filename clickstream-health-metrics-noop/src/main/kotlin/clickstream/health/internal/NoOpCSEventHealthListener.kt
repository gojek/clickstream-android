package clickstream.health.internal

import clickstream.health.intermediate.CSEventHealthListener
import clickstream.health.model.CSEventHealth

internal class NoOpCSEventHealthListener : CSEventHealthListener {
    override fun onEventCreated(healthEvent: CSEventHealth) {
        /*NoOp*/
    }
}
