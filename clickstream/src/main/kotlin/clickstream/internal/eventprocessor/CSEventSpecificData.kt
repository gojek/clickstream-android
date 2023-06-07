package clickstream.internal.eventprocessor

/**
 * Data class which contains event specific attributes for an event.
 *
 * @param eventGUID
 * @param timeStamp
 */
internal data class CSEventSpecificData(
    val eventGUID: String,
    val timeStamp: Long
)

/**
 * This is being used to represent priority of any event
 */
internal enum class CSEventPriority {
    REALTIME,
    NON_REALTIME
}
