package clickstream

import clickstream.internal.CSEventInternal
import com.google.protobuf.Timestamp

/***
 * CSBytesEvent is a wrapper which holds guid, timestamp,
 * event name and ByteArray of the MessageLite
 */
public data class CSBytesEvent(
    val guid: String,
    val timestamp: Timestamp,
    val eventName: String,
    val eventData: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CSBytesEvent

        if (guid != other.guid) return false
        if (timestamp != other.timestamp) return false
        if (eventName != other.eventName) return false
        if (!eventData.contentEquals(other.eventData)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = guid.hashCode()
        result = 31 * result + timestamp.hashCode()
        result = 31 * result + eventName.hashCode()
        result = 31 * result + eventData.contentHashCode()
        return result
    }
}

internal fun CSBytesEvent.toInternal(): CSEventInternal {
    return CSEventInternal.CSBytesEvent(
        guid, timestamp,
        eventName, eventData
    )
}