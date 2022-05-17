package clickstream.health

import clickstream.health.model.CSHealthEvent

public interface DefaultCSHealthListener {
    public fun onReceived(healthEvent: CSHealthEvent)
}