package clickstream.internal.eventscheduler

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

    /**
     * A function to retrieve event data based on given batch ID
     */
    suspend fun loadEventsByRequestId(eventBatchGuId: String): List<CSEventData>

    /**
     * A function to retrieve first n events data
     */
    suspend fun getUnprocessedEventsWithLimit(limit: Int): List<CSEventData>

    /**
     * A function to update all the event data into the DB
     */
    suspend fun updateEventDataList(eventDataList: List<CSEventData>)

    /**
     * A function to return all events with are not ongoing (isOnGoing flag is false)
     */
    suspend fun getAllUnprocessedEvents(): List<CSEventData>

    /**
     * A function to return the count of all events which are not ongoing (isOnGoing flag is false)
     */
    suspend fun getAllUnprocessedEventsCount(): Int

    /**
     * Returns current count of events in DB
     * */
    suspend fun getEventCount(): Int
}
