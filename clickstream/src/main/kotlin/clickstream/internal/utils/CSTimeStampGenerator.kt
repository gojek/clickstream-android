package clickstream.internal.utils

import clickstream.config.timestamp.CSEventGeneratedTimestampListener

/**
 * Generate the current time stamp
 */
internal interface CSTimeStampGenerator {
    /**
     * Returns the current time stamp at he given instant
     */
    fun getTimeStamp(): Long
}

/**
 * Implementation of the [CSTimeStampGenerator]
 */
internal class DefaultCSTimeStampGenerator(
    private val timestampListener: CSEventGeneratedTimestampListener
) : CSTimeStampGenerator {

    override fun getTimeStamp(): Long {
        return runCatching {
            timestampListener.now()
        }.getOrDefault(System.currentTimeMillis())
    }
}
