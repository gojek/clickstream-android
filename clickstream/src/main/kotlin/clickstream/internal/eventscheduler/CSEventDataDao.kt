package clickstream.internal.eventscheduler

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * A collection of function to accommodate and Communicates with the implementation of DAO.
 * Thread switching should be handled by the caller side in they implementation scope.
 */
@Dao
internal interface CSEventDataDao {

    /**
     * A function [insert] that accommodate an action to save a [T] object to persistence of choice.
     *
     * **Note:**
     * Thread switching must be handled by the caller side. e.g wrapped in form of [IO]
     *
     * @param eventData - Event Data to be stored
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(eventData: CSEventData)

    /**
     * A function [insertAll] that accommodate an action to save a [List] of [T] object to persistence of choice.
     *
     * **Note:**
     * Thread switching must be handled by the caller side. e.g wrapped in form of [IO]
     *
     * @param eventDataList - List of Event Data to be stored
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(eventDataList: List<CSEventData>)

    /**
     * A function [loadAll] that accommodate an action to retrieve all of [T] object from persistence of choice.
     *
     * **Note:**
     * Thread switching must be handled by the caller side. e.g wrapped in form of [IO]
     *
     */
    @Query("SELECT * FROM EventData WHERE isOnGoing = 0 ORDER BY eventTimeStamp DESC")
    fun loadAll(): Flow<List<CSEventData>>

    /**
     * A function [getAll] that accommodate an action to retrieve all of [T] object from persistence of choice.
     *
     * **Note:**
     * Thread switching must be handled by the caller side. e.g wrapped in form of [IO]
     *
     */
    @Query("SELECT * FROM EventData ORDER BY eventTimeStamp DESC")
    fun getAll(): List<CSEventData>

    /**
     * A suspended function [loadOnGoingEvents] that accommodate an action to
     * retrieve all of [T] object from persistence of choice.
     *
     * **Note:**
     * Thread switching must be handled by the caller side. e.g wrapped in form of [IO]
     *
     */
    @Query("SELECT * FROM EventData WHERE isOnGoing = 1 ORDER BY eventTimeStamp DESC")
    fun loadOnGoingEvents(): List<CSEventData>

    /**
     * A suspended function [setOnGoingEvent] that accommodate an action to
     * update the ongoing field for the given guid
     *
     * **Note:**
     * Thread switching must be handled by the caller side. e.g wrapped in form of [IO]
     */
    @Query("UPDATE EventData SET isOnGoing=:ongoing WHERE eventRequestGuid =:guid ")
    fun setOnGoingEvent(guid: String, ongoing: Boolean)

    /**
     * A function [deleteBy] that accommodate an action to delete of [T] object from persistence of choice
     * by given the key of [String].
     *
     * **Note:**
     * Thread switching must be handled by the caller side. e.g wrapped in form of [IO]
     *
     * @param eventBatchGuId - The Batch for group of events
     */
    @Query("DELETE FROM EventData WHERE eventRequestGuid = :eventBatchGuId")
    fun deleteByGuId(eventBatchGuId: String)
}
