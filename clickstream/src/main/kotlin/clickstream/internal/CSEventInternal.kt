package clickstream.internal

import com.google.protobuf.MessageLite
import com.google.protobuf.Timestamp

internal sealed class CSEventInternal {
    data class CSEvent(
        val guid: String,
        val timestamp: Timestamp,
        val message: MessageLite
    ) : CSEventInternal()
    data class CSBytesEvent(
        val guid: String,
        val timestamp: Timestamp,
        val eventName: String,
        val eventData: ByteArray
    ) : CSEventInternal()
}