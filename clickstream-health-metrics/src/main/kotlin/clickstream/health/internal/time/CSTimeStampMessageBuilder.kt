package clickstream.health.internal.time

import com.google.protobuf.Timestamp

internal const val MILLI_TO_SECONDS: Int = 1000
internal const val MILLI_TO_NANO: Int = 1000000

/**
 * Generates the TimeStamp message for the given time
 */
internal object CSTimeStampMessageBuilder {

    /**
     * Generates the TimeStamp message for the given time
     *
     * @param millis - time for which timestamp is constructed.
     */
    internal fun build(millis: Long): Timestamp {
        return Timestamp.newBuilder()
            .setSeconds(millis / MILLI_TO_SECONDS)
            .setNanos(((millis % MILLI_TO_SECONDS) * MILLI_TO_NANO).toInt())
            .build()!!
    }
}
