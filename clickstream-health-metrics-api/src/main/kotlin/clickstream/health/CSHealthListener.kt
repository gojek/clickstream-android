package clickstream.health

import clickstream.health.model.CSHealthEvent

public interface CSHealthListener {
    public fun onReceived(healthEvent: CSHealthEvent)
}