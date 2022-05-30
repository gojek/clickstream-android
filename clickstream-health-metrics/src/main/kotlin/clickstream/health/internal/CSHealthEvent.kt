package clickstream.health.internal

import androidx.room.Entity
import androidx.room.PrimaryKey
import clickstream.health.constant.CSEventNamesConstant.ClickStreamEventBatchCreated
import clickstream.health.model.CSHealthEventDTO
import clickstream.health.constant.CSHealthKeysConstant
import clickstream.health.constant.CSEventTypesConstant

/**
 * The data class for health events which will be stored to the DB
 *
 * @param healthEventID - The unique ID for each health event
 * @param eventName - The event name for this event, will be one of [CSEventNames]
 * @param eventType - The event type for this event, will be one of [CSEventTypesConstant]
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
internal data class CSHealthEvent(
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

    companion object {
        fun CSHealthEventDTO.dtoMapTo(): CSHealthEvent {
            return CSHealthEvent(
                healthEventID = healthEventID,
                eventName = eventName,
                eventType = eventType,
                timestamp = timestamp,
                eventId = eventId,
                eventBatchId = eventBatchId,
                error = error,
                sessionId = sessionId,
                count = count,
                networkType = networkType,
                startTime = startTime,
                stopTime = stopTime,
                bucketType = bucketType,
                batchSize = batchSize,
                appVersion = appVersion
            )
        }

        fun CSHealthEvent.mapToDto(): CSHealthEventDTO {
            return CSHealthEventDTO(
                healthEventID = healthEventID,
                eventName = eventName,
                eventType = eventType,
                timestamp = timestamp,
                eventId = eventId,
                eventBatchId = eventBatchId,
                error = error,
                sessionId = sessionId,
                count = count,
                networkType = networkType,
                startTime = startTime,
                stopTime = stopTime,
                bucketType = bucketType,
                batchSize = batchSize,
                appVersion = appVersion
            )
        }

        fun List<CSHealthEvent>.mapToDtos(): List<CSHealthEventDTO> {
            return this.map { healthEvent -> healthEvent.mapToDto() }
        }

        fun List<CSHealthEventDTO>.dtosMapTo(): List<CSHealthEvent> {
            return this.map { healthEvent -> healthEvent.dtoMapTo() }
        }
    }

    /**
     * Returns HashMap of event data which is used for health & performance tracking.     *
     */
    public fun eventData(): HashMap<String, Any> {
        val eventData: HashMap<String, Any> = hashMapOf()

        if (eventType.isNotBlank()) {
            eventData[CSHealthKeysConstant.EVENT_TYPE] = eventType
        }

        if (sessionId.isNotBlank()) {
            eventData[CSHealthKeysConstant.SESSION_ID] = sessionId
        }

        if (error.isNotBlank()) {
            eventData[CSHealthKeysConstant.REASON] = error
        }

        if (eventId.isNotBlank()) {
            eventData[getEventIdKey()] = eventId
        }

        if (eventBatchId.isNotBlank()) {
            eventData[getEvenBatchIdKey()] = eventBatchId
        }
        if (timestamp.isNotBlank()) {
            eventData[CSHealthKeysConstant.TIMESTAMP] = timestamp
        }

        if (count != 0) {
            eventData[CSHealthKeysConstant.COUNT] = count
        }

        if (bucketType.isNotBlank()) {
            eventData[CSHealthKeysConstant.BUCKET] = bucketType
        }

        return eventData
    }

    private fun getEventIdKey(): String =
        when (eventType) {
            CSEventTypesConstant.INSTANT -> {
                if (eventName == ClickStreamEventBatchCreated.value) {
                    CSHealthKeysConstant.EVENTS
                } else {
                    CSHealthKeysConstant.EVENT_ID
                }
            }
            CSEventTypesConstant.AGGREGATE -> CSHealthKeysConstant.EVENTS
            CSEventTypesConstant.BUCKET -> CSHealthKeysConstant.EVENTS
            else -> CSHealthKeysConstant.EVENTS
        }

    private fun getEvenBatchIdKey(): String =
        when (eventType) {
            CSEventTypesConstant.INSTANT -> CSHealthKeysConstant.EVENT_BATCH_ID
            CSEventTypesConstant.AGGREGATE -> CSHealthKeysConstant.EVENT_BATCHES
            CSEventTypesConstant.BUCKET -> CSHealthKeysConstant.EVENT_BATCHES
            else -> CSHealthKeysConstant.EVENT_BATCHES
        }
}
