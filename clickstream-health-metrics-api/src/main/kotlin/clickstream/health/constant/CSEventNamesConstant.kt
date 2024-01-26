package clickstream.health.constant

import androidx.annotation.RestrictTo

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public enum class CSHealthEventName(public val value: String) {

    // Batch sent events
    ClickStreamBatchSent("ClickStream Batch Sent"),
    ClickStreamEventBatchTriggerFailed("Clickstream Event Batch Trigger Failed"),
    ClickStreamBatchWriteFailed("ClickStream Write to Socket Failed"),

    // Socket connection events
    ClickStreamConnectionFailed("ClickStream Connection Failed"),

    // Batch acknowledgement events
    ClickStreamEventBatchErrorResponse("Clickstream Event Batch Error response"),
    ClickStreamEventBatchAck("Clickstream Event Batch Success Ack"),
    ClickStreamEventBatchTimeout("Clickstream Event Batch Timeout"),

    // Flush events
    ClickStreamFlushOnBackground("ClickStream Flush On Background"),
    ClickStreamFlushOnForeground("Clickstream Flush On Foreground"),
}

