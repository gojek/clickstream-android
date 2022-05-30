package clickstream.health

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * The data class for health events which will be stored to the DB
 *
 * @param healthEventID - The unique ID for each health event
 * @param eventName - The event name for this event, will be one of [CSEventNames]
 * @param eventType - The event type for this event, will be one of [EventTypes]
 * @param timestamp - The timestamp at which the event was created
 * @param eventId - The event ID for which this health event for created
 * @param eventBatchId - The event batch ID for which this health event for created
 * @param error - The event type for this event, will be one of [CSErrorReasons]
 * @param sessionId - The sessionId for current app session
 * @param count - The count of events for aggregated events
 * @param networkType - The network type when event occurred
 * @param startTime - The time to be captured for a performance event
 * @param stopTime - The time to be captured for a performance event
 * @param bucketType - The bucket type for a performance event
 * @param batchSize - The size of an event batch
 * @param appVersion - Host application version that internally we used for measure drop rates.
 */
@Entity(tableName = "HealthStats")
public data class CSHealthEvent(
    @PrimaryKey(autoGenerate = true)
    val healthEventID: Int = 0,
    val eventName: String,
    val eventType: String,
    val timestamp: String = "",
    val eventId: String = "",
    val eventBatchId: String = "",
    val error: String = "",
    val sessionId: String = "",
    val count: Int = 0,
    val networkType: String = "",
    val startTime: Long = 0L,
    val stopTime: Long = 0L,
    val bucketType: String = "",
    val batchSize: Long = 0L,
    val appVersion: String
) {
    /**
     * Returns HashMap of event data which is used for health & performance tracking.     *
     */
    public fun eventData(): HashMap<String, Any> {
        val eventData: HashMap<String, Any> = hashMapOf()

        if (eventType.isNotBlank()) {
            eventData[CSHealthKeys.EVENT_TYPE] = eventType
        }

        if (sessionId.isNotBlank()) {
            eventData[CSHealthKeys.SESSION_ID] = sessionId
        }

        if (error.isNotBlank()) {
            eventData[CSHealthKeys.REASON] = error
        }

        if (eventId.isNotBlank()) {
            eventData[getEventIdKey()] = eventId
        }

        if (eventBatchId.isNotBlank()) {
            eventData[getEvenBatchIdKey()] = eventBatchId
        }
        if (timestamp.isNotBlank()) {
            eventData[CSHealthKeys.TIMESTAMP] = timestamp
        }

        if (count != 0) {
            eventData[CSHealthKeys.COUNT] = count
        }

        if (bucketType.isNotBlank()) {
            eventData[CSHealthKeys.BUCKET] = bucketType
        }

        return eventData
    }

    private fun getEventIdKey(): String =
        when (eventType) {
            EventTypes.INSTANT -> {
                if (eventName == CSEventNames.ClickStreamEventBatchCreated.value) {
                    CSHealthKeys.EVENTS
                } else {
                    CSHealthKeys.EVENT_ID
                }
            }
            EventTypes.AGGREGATE -> CSHealthKeys.EVENTS
            EventTypes.BUCKET -> CSHealthKeys.EVENTS
            else -> CSHealthKeys.EVENTS
        }

    private fun getEvenBatchIdKey(): String =
        when (eventType) {
            EventTypes.INSTANT -> CSHealthKeys.EVENT_BATCH_ID
            EventTypes.AGGREGATE -> CSHealthKeys.EVENT_BATCHES
            EventTypes.BUCKET -> CSHealthKeys.EVENT_BATCHES
            else -> CSHealthKeys.EVENT_BATCHES
        }
}
