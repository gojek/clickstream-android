package clickstream.health.internal

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

internal const val INSTANT_EVENT_TYPE = "instant"
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
    suspend fun insert(healthEvent: CSHealthEventEntity)

    /**
     * A function [insertAll] that accommodate an action to save a [List] of [CSHealthEventEntity] object.
     *
     * **Note:**
     * Thread switching must be handled by the caller side. e.g wrapped in form of [IO]
     *
     * @param healthEventList - List of Event Data to be stored
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(healthEventList: List<CSHealthEventEntity>)

    /**
     * A function [getEventByType] that retrieves all events based on the event type.
     *
     * **Note:**
     * Thread switching must be handled by the caller side. e.g wrapped in form of [IO]
     *
     */
    @Query("SELECT * FROM HealthStats WHERE eventType = :eventType")
    suspend fun getEventByType(eventType: String): List<CSHealthEventEntity>

    /**
     * A function [deleteBySessionId] that accommodate an action to delete of [CSHealthEventEntity] objects
     * by given sessionId.
     *
     * **Note:**
     * Thread switching must be handled by the caller side. e.g wrapped in form of [IO]
     *
     * @param sessionId - The sessionId for group of health events
     */
    @Query("DELETE FROM HealthStats WHERE sessionId = :sessionId and stopTime >= startTime")
    suspend fun deleteBySessionId(sessionId: String)

    /**
     *  function [deleteHealthEvent] that accommodate an action to delete of [CSHealthEventEntity] objects.
     *
     * **Note:**
     * Thread switching must be handled by the caller side
     */
    @Delete
    suspend fun deleteHealthEvent(events: List<CSHealthEventEntity>)
}
