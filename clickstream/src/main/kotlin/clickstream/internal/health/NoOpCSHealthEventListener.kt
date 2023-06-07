package clickstream.internal.health

import clickstream.health.intermediate.CSEventHealthListener
import clickstream.health.model.CSEventHealth

internal class NoOpCSHealthEventListener : CSEventHealthListener {

    override fun onEventCreated(healthEvent: CSEventHealth) {
        /*NoOp*/
    }
}