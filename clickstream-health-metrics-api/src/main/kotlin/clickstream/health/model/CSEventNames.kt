package clickstream.health.model

public enum class CSEventNames(public val value: String) {
    ClickStreamFailedInit("ClickStream Failed Init"),
    ClickStreamEventReceived("Clickstream Event Received"),
    ClickStreamEventObjectCreated("Clickstream Event Object Created"),
    ClickStreamEventCached("Clickstream Event Cached"),
    ClickStreamEventBatchCreated("Clickstream Event Batch Created"),
    ClickStreamEventBatchTriggerFailed("Clickstream Event Batch Trigger Failed"),
    ClickStreamEventBatchErrorResponse("Clickstream Event Batch Error response"),
    ClickStreamBatchSent("ClickStream Batch Sent"),
    ClickStreamBatchWriteFailed("ClickStream Write to Socket Failed"),
    ClickStreamConnectionFailed("ClickStream Connection Failed"),
    ClickStreamEventBatchAck("Clickstream Event Batch Success Ack"),
    ClickStreamEventBatchTimeout("Clickstream Event Batch Timeout"),
    ClickStreamEventBatchLatency("ClickStream Event Batch Latency"),
    ClickStreamEventWaitTime("ClickStream Event Wait Time"),
    ClickStreamEventBatchWaitTime("ClickStream Event Batch Wait Time"),
    ClickStreamBatchSize("ClickStream Batch Size"),
    ClickStreamFlushOnBackground("ClickStream Flush On Background"),
    ClickStreamBackgroundServiceCompleted("ClickStream Background Service Completed"),
    ClickStreamInvalidMessage("Clickstream Event Unsupported String Received")
}