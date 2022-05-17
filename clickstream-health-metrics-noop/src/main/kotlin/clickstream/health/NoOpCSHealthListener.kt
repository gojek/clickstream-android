package clickstream.health

import clickstream.health.model.CSHealthEvent

public interface NoOpCSHealthListener {
    public fun onReceived(healthEvent: CSHealthEvent)
}