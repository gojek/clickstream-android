package clickstream.health.internal

import androidx.annotation.RestrictTo
import androidx.room.Entity
import androidx.room.PrimaryKey
import clickstream.health.constant.CSEventTypesConstant
import clickstream.health.model.CSHealthEvent
import clickstream.health.model.CSHealthEventDTO

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
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Entity(tableName = "HealthStats")
public data class CSHealthEventEntity(
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
    val appVersion: String,
    val timeToConnection: Long = 0L
) {

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    public companion object {
        @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
        public fun CSHealthEventDTO.dtoMapTo(): CSHealthEventEntity {
            return CSHealthEventEntity(
                healthEventID = healthEventID,
                eventName = eventName,
                eventType = eventType,
                timestamp = timestamp,
                eventId = eventGuid,
                eventBatchId = eventBatchGuid,
                error = error,
                sessionId = sessionId,
                count = count,
                networkType = networkType,
                startTime = startTime,
                stopTime = stopTime,
                bucketType = bucketType,
                batchSize = batchSize,
                appVersion = appVersion,
                timeToConnection = timeToConnection
            )
        }

        @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
        public fun CSHealthEventEntity.mapToDto(): CSHealthEventDTO {
            return CSHealthEventDTO(
                healthEventID = healthEventID,
                eventName = eventName,
                eventType = eventType,
                timestamp = timestamp,
                eventGuid = eventId,
                eventBatchGuid = eventBatchId,
                error = error,
                sessionId = sessionId,
                count = count,
                networkType = networkType,
                startTime = startTime,
                stopTime = stopTime,
                bucketType = bucketType,
                batchSize = batchSize,
                appVersion = appVersion,
                timeToConnection = timeToConnection
            )
        }

        @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
        public fun List<CSHealthEventEntity>.mapToDtos(): List<CSHealthEventDTO> {
            return this.map { healthEvent -> healthEvent.mapToDto() }
        }

        @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
        public fun List<CSHealthEventDTO>.dtosMapTo(): List<CSHealthEventEntity> {
            return this.map { healthEvent -> healthEvent.dtoMapTo() }
        }
    }

    public fun mapToHealthEventDTO(): CSHealthEvent {
        return CSHealthEvent(
            eventName = eventName,
            sessionId = sessionId,
            eventGuids = if (eventId.isNotBlank()) eventId.split(",") else null,
            eventBatchGuids = if (eventBatchId.isNotBlank()) eventBatchId.split(",") else null,
            failureReason = error,
            timeToConnection = timeToConnection,
            eventCount = count
        )
    }
}
