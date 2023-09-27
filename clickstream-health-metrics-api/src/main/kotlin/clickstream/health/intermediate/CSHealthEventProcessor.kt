package clickstream.health.intermediate

import clickstream.health.model.CSEventForHealth
import clickstream.health.model.CSHealthEvent
import com.gojek.clickstream.internal.Health
import kotlinx.coroutines.flow.Flow

/**
 * Class dealing with health events. It has following responsibilities :
 *
 * 1) Logging clickstream health events.
 * 2) Returning stream of health events for processing.
 * 3) Sending health event data to upstream listener.
 *
 *
 * */
public interface CSHealthEventProcessor {

    /**
     * Process non batch health events (like socket connection).
     * These events do not require batchSize related data.
     *
     * @param csEvent: [CSHealthEvent] instance to be logged.
     * */
    public suspend fun insertNonBatchEvent(csEvent: CSHealthEvent): Boolean

    /**
     * Process batch events health events (like batch failed, success).
     *
     * @param csEvent: [CSHealthEvent] instance to be logged.
     * @param list: [CSEventForHealth] list required for tracking data like eventCount, guids etc.
     *
     * */
    public suspend fun insertBatchEvent(
        csEvent: CSHealthEvent,
        list: List<CSEventForHealth>
    ): Boolean

    /**
     * Process batch events health events (like batch failed, success). Call this if only batchSize
     * matters.
     *
     * @param csEvent: [CSHealthEvent] instance to be logged.
     * @param eventCount: number of events in the batch.
     *
     * */
    public suspend fun insertBatchEvent(
        csEvent: CSHealthEvent,
        eventCount: Long,
    ): Boolean

    /**
     * Returns flow of health events with type.
     *
     * */
    public fun getHealthEventFlow(type: String, deleteEvents: Boolean = false): Flow<List<Health>>


    /**
     * Pushing events to upstream listener.
     *
     */
    public suspend fun pushEventToUpstream(type: String, deleteEvents: Boolean)
}