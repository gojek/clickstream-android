package clickstream.internal.eventscheduler

import kotlinx.coroutines.flow.Flow

/**
 * The Storage Repository communicates between storage/cache and the Scheduler
 * It read, writes, deletes the data in the storage
 */
internal interface CSEventRepository {

    /**
     * A function to insert the event data into the DB
     */
    suspend fun insertEventData(eventData: CSEventData)

    /**
     * A function to insert all the event data into the DB
     */
    suspend fun insertEventDataList(eventDataList: List<CSEventData>)

    /**
     * A function to retrieve all the un processed events in the cache
     */
    suspend fun getEventDataList(): Flow<List<CSEventData>>

    /**
     * A function to retrieve all the unprocessed events in the cache
     */
    suspend fun getAllEvents(): List<CSEventData>

    /**
     * A suspend function to retrieve all the un processed events in the cache
     */
    suspend fun getOnGoingEvents(): List<CSEventData>

    /**
     * A suspend function which resets the ongoing flag for the given batch guid
     */
    suspend fun resetOnGoingForGuid(guid: String)

    /**
     * A function to remove event data based on the ID given
     */
    suspend fun deleteEventDataByGuId(eventBatchGuId: String)

    suspend fun getEventsOnGuId(eventBatchGuId: String) : List<CSEventData>
}
