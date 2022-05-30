package clickstream.health

/**
 * The HealthRepository acts as a wrapper above storage/cache.
 * It read, writes, deletes the data in the storage
 */
public interface CSHealthEventRepository {

    /**
     * A function to insert the health event into the DB
     */
    public suspend fun insertHealthEvent(healthEvent: CSHealthEvent)

    /**
     * A function to insert the health event list into the DB
     */
    public suspend fun insertHealthEventList(healthEventList: List<CSHealthEvent>)

    /**
     * A function to retrieve all the instant health events in the DB
     */
    public suspend fun getInstantEvents(): List<CSHealthEvent>

    /**
     * A function to retrieve all the bucket health events in the DB
     */
    public suspend fun getBucketEvents(): List<CSHealthEvent>

    /**
     * A function to retrieve all the aggregate health events in the DB
     */
    public suspend fun getAggregateEvents(): List<CSHealthEvent>

    /**
     * A function to delete all the health events for a sessionID
     */
    public suspend fun deleteHealthEventsBySessionId(sessionId: String)

    /**
     * A function to delete the given health events
     */
    public suspend fun deleteHealthEvents(events: List<CSHealthEvent>)
}
