package clickstream

import com.google.protobuf.MessageLite
import com.google.protobuf.Timestamp

/***
 * ClickStreamEvent is a wrapper which holds guid, timestamp and message
 */
public data class CSEvent(
    val guid: String,
    val timestamp: Timestamp,
    val message: MessageLite
)
