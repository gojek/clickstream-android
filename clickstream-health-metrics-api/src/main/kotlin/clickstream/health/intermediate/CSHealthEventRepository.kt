package clickstream.health.intermediate

import clickstream.health.model.CSHealthEventDTO

/**
 * The HealthRepository acts as a wrapper above storage/cache.
 * It read, writes, deletes the data in the storage
 */
public interface CSHealthEventRepository {

    /**
     * A function to insert the health event into the DB
     */
    public suspend fun insertHealthEvent(healthEvent: CSHealthEventDTO)

    /**
     * A function to insert the health event list into the DB
     */
    public suspend fun insertHealthEventList(healthEventList: List<CSHealthEventDTO>)

    /**
     * A function to retrieve all the instant health events in the DB
     */
    public suspend fun getInstantEvents(): List<CSHealthEventDTO>

    /**
     * A function to retrieve all the bucket health events in the DB
     */
    public suspend fun getBucketEvents(): List<CSHealthEventDTO>

    /**
     * A function to retrieve all the aggregate health events in the DB
     */
    public suspend fun getAggregateEvents(): List<CSHealthEventDTO>

    /**
     * A function to delete all the health events for a sessionID
     */
    public suspend fun deleteHealthEventsBySessionId(sessionId: String)

    /**
     * A function to delete the given health events
     */
    public suspend fun deleteHealthEvents(events: List<CSHealthEventDTO>)
}
