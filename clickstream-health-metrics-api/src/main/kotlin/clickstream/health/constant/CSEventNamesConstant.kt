package clickstream.health.constant

import androidx.annotation.RestrictTo

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public enum class CSEventNamesConstant(public val value: String) {;

    public enum class Instant(public val value: String) {
        /**
         * Tracks the instances where the connection gets dropped.
         *
         * Type: Instant
         * Priority: Critical
         */
        ClickStreamConnectionDropped("Clickstream Connection Dropped"),

        /**
         * Tracks the connection attempt instances.
         *
         * Type: Instant
         * Priority: Critical
         */
        ClickStreamConnectionAttempt("Clickstream Connection Attempt"),

        /**
         * Tracks the connection attempt success instances.
         *
         * Type: Instant
         * Priority: Critical
         */
        ClickStreamConnectionSuccess("Clickstream Connection Success"),

        /**
         * Tracks the connection attempt failure instances.
         *
         * Type: Instant
         * Priority: Critical
         */
        ClickStreamConnectionFailure("Clickstream Connection Failure"),

        /**
         * Tracks the instances when the clickstream event batch gets timed out.
         *
         * Type: Instant
         * Priority: Critical
         */
        ClickStreamEventBatchTimeout("Clickstream Event Batch Timeout"),

        /**
         * Tracks the instances when the clickstream event batch fails to get written on the socket.
         *
         * Type: Instant
         * Priority: Critical
         */
        ClickStreamWriteToSocketFailed("ClickStream Write to Socket Failed"),

        /**
         * Tracks the instances when the batch fails to get triggered.
         *
         * Type: Instant
         * Priority: Low
         */
        ClickStreamEventBatchTriggerFailed("Clickstream Event Batch Trigger Failed"),

        /**
         * Tracks the instances when the clickstream request results in a error response.
         *
         * Type: Instant
         * Priority: Critical
         */
        ClickStreamEventBatchErrorResponse("Clickstream Event Batch Error response"),
    }

    public enum class Flushed(public val value: String) {
        /**
         * This event is track the drop rate comparison only and not the part of the funnel.
         * Would be triggered for the event which is used to track the drops Eg. `AdCardEvent`
         *
         * Type: Flushed
         * Priority: Critical
         */
        ClickStreamEventReceivedForDropRate("Clickstream Event Received For Drop Rate"),

        /**
         * Tracks the instances where the event is received by the Clickstream library
         *
         * Type: Flushed
         * Priority: Critical
         */
        ClickStreamEventReceived("Clickstream Event Received"),
    }

    public enum class AggregatedAndFlushed(public val value: String) {
        /**
         * Tracks the instances when the clickstream event object is cached.
         *
         * Type: Aggregated and Flushed
         * Priority: Low
         */
        ClickStreamEventCached("Clickstream Event Cached"),

        /**
         * Tracks the instances when the clickstream event batch is created.
         *
         * Type: Aggregated and Flushed
         * Priority: Low
         */
        ClickStreamEventBatchCreated("Clickstream Event Batch Created"),

        /**
         * Tracks the instances when the clickstream batch gets successfully sent to raccoon.
         *
         * Type: Aggregated and Flushed
         * Priority: Low
         */
        ClickStreamBatchSent("ClickStream Batch Sent"),

        /**
         * Tracks the instances when raccoon acks the event request.
         *
         * Type: Aggregated and Flushed
         * Priority: Low
         */
        ClickStreamEventBatchSuccessAck("Clickstream Event Batch Success Ack"),

        /**
         * Tracks the instances when the clickstream batches are flushed on background.
         *
         * Type: Aggregated and Flushed
         * Priority: Critical
         */
        ClickStreamFlushOnBackground("ClickStream Flush On Background"),

        /**
         * Tracks the instances when the clickstream batches are flushed on background.
         *
         * Type: Aggregated and Flushed
         * Priority: Critical
         */
        ClickStreamFlushOnForeground("ClickStream Flush On Foreground"),
    }
}

