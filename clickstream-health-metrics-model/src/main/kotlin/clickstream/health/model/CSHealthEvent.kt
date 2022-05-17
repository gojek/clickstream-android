package clickstream.health.model

public data class CSHealthEvent(
    private val eventName: String,
    private val eventType: CSHealthEventType,
    private val eventBatchId: String,
    private val eventId: String
)