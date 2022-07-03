package clickstream.logger

import androidx.annotation.RestrictTo

/**
 * A class to declare constant tag
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public object CSLogConstant {
    public const val CLICK_STREAM_LOG_TAG: String = "ClickStream"
    public const val BATCH_SENT_TAG: String = "ClickStream Batch Sent"
    public const val BATCH_ACK_SUCCESS_TAG: String = "ClickStream Batch Success Ack"
    public const val BATCH_ACK_FAILURE_TAG: String = "ClickStream Batch Failed Ack"
    public const val BATCH_REQUEST_ACK_TIMEOUT_TAG: String = "ClickStream Batch Ack Timeout"
    public const val FAILED_TO_INIT_TAG: String = "ClickStream Failed Init"
    public const val EVENT_BATCH_GUID_KEY: String = "event_batch_guid"
    public const val EVENT_GUID_LIST_KEY: String = "events"
    public const val EVENT_RETRY_COUNT: String = "retry_count"
}
