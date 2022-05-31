package clickstream.health.model

/**
 * A data class which expose event meta.
 */
public data class CSEventHealth(
    val eventGuid: String,
    val eventTimeStamp: Long,
    val messageSerializedSizeInBytes: Int,
    val messageName: String,
    val eventName: String
)
