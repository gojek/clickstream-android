package clickstream.health

import com.google.protobuf.Timestamp

internal const val MILLI_TO_SECONDS: Int = 1000
internal const val MILLI_TO_NANO: Int = 1000000

/**
 * Generates the TimeStamp message for the given time
 */
public object CSTimeStampMessageBuilder {

    /**
     * Generates the TimeStamp message for the given time
     *
     * @param millis - time for which timestamp is constructed.
     */
    public fun build(millis: Long): Timestamp =
        Timestamp.newBuilder()
            .setSeconds(millis / MILLI_TO_SECONDS)
            .setNanos(((millis % MILLI_TO_SECONDS) * MILLI_TO_NANO).toInt())
            .build()!!
}
