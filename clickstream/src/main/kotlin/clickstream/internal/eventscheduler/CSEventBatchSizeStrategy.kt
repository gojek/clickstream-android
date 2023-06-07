package clickstream.internal.eventscheduler

import clickstream.internal.CSEventInternal

public const val DEFAULT_MIN_EVENT_COUNT: Int = 20
internal interface CSEventBatchSizeStrategy {
    suspend fun regulatedCountOfEventsPerBatch(expectedBatchSize: Int): Int
    fun logEvent(event: CSEventInternal) { /*NO-OP*/ }
}