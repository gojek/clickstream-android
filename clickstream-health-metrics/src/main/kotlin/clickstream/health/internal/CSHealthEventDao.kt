package clickstream.health.internal

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import clickstream.health.internal.CSHealthEvent

internal const val INSTANT_EVENT_TYPE = "instant"
internal const val BUCKET_EVENT_TYPE = "bucket"
internal const val AGGREGATE_EVENT_TYPE = "aggregate"

/**
 * A collection of function to accommodate and Communicates with the implementation of DAO.
 * Thread switching should be handled by the caller side in they implementation scope.
 */
@Dao
internal interface CSHealthEventDao {

    /**
     * A function [insert] that accommodate an action to insert an sdk health event.
     *
     * **Note:**
     * Thread switching must be handled by the caller side. e.g wrapped in form of [IO]
     *
     * @param healthEvent - Event Data to be stored
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(healthEvent: CSHealthEvent)

    /**
     * A function [insertAll] that accommodate an action to save a [List] of [CSHealthEvent] object.
     *
     * **Note:**
     * Thread switching must be handled by the caller side. e.g wrapped in form of [IO]
     *
     * @param healthEventList - List of Event Data to be stored
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(healthEventList: List<CSHealthEvent>)

    /**
     * A function [getEventByType] that retrieves all events based on the event type.
     *
     * **Note:**
     * Thread switching must be handled by the caller side. e.g wrapped in form of [IO]
     *
     */
    @Query("SELECT * FROM HealthStats WHERE eventType = :eventType")
    fun getEventByType(eventType: String): List<CSHealthEvent>

    /**
     * A function [getBucketEventByEventBatchId] that accommodate an action to get list of [CSHealthEvent] objects
     * by given event name and batch ID.
     */
    @Query("SELECT * FROM HealthStats WHERE eventType = 'bucket' and eventName = :eventName and eventBatchId = :eventBatchId")
    fun getBucketEventByEventBatchId(
        eventName: String,
        eventBatchId: String
    ): List<CSHealthEvent>

    /**
     * A function [getBucketEventsByEventIdList] that accommodate an action to get list of [CSHealthEvent] objects
     * by given event name and event ID.
     */
    @Query("SELECT * FROM HealthStats WHERE eventType = 'bucket' and eventName = :eventName and eventId in (:eventId)")
    fun getBucketEventsByEventIdList(
        eventName: String,
        eventId: List<String>
    ): List<CSHealthEvent>

    /**
     * A function [deleteBySessionId] that accommodate an action to delete of [CSHealthEvent] objects
     * by given sessionId.
     *
     * **Note:**
     * Thread switching must be handled by the caller side. e.g wrapped in form of [IO]
     *
     * @param sessionId - The sessionId for group of health events
     */
    @Query("DELETE FROM HealthStats WHERE sessionId = :sessionId and stopTime >= startTime")
    fun deleteBySessionId(sessionId: String)

    /**
     *  function [deleteHealthEvent] that accommodate an action to delete of [CSHealthEvent] objects.
     *
     * **Note:**
     * Thread switching must be handled by the caller side
     */
    @Delete
    fun deleteHealthEvent(events: List<CSHealthEvent>)
}
