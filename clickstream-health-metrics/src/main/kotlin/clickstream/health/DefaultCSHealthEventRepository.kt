package clickstream.health

/**
 * The HealthRepositoryImpl is the implementation detail of the [CSHealthEventRepository].
 *
 * @param healthEventDao - The Dao object to communicate to the DB
 */
public class DefaultCSHealthEventRepository(
    private val sessionId: String,
    private val healthEventDao: CSHealthEventDao,
    private val info: CSInfo
) : clickstream.health.CSHealthEventRepository {

    override suspend fun insertHealthEvent(healthEvent: clickstream.health.CSHealthEvent) {
        val event = healthEvent.copy(
            sessionId = sessionId,
            timestamp = System.currentTimeMillis().toString(),
            appVersion = info.appInfo.appVersion
        )
        healthEventDao.insert(healthEvent = event)
    }

    override suspend fun insertHealthEventList(healthEventList: List<clickstream.health.CSHealthEvent>) {
        val eventList = healthEventList.map {
            it.copy(
                sessionId = sessionId,
                timestamp = System.currentTimeMillis().toString()
            )
        }.toList()
        healthEventDao.insertAll(healthEventList = eventList)
    }

    override suspend fun getInstantEvents(): List<clickstream.health.CSHealthEvent> {
        return healthEventDao.getEventByType(INSTANT_EVENT_TYPE)
    }

    override suspend fun getBucketEvents(): List<clickstream.health.CSHealthEvent> {
        return healthEventDao.getEventByType(BUCKET_EVENT_TYPE)
    }

    override suspend fun getAggregateEvents(): List<clickstream.health.CSHealthEvent> {
        return healthEventDao.getEventByType(AGGREGATE_EVENT_TYPE)
    }

    override suspend fun deleteHealthEventsBySessionId(sessionId: String) {
        healthEventDao.deleteBySessionId(sessionId = sessionId)
    }

    override suspend fun deleteHealthEvents(events: List<clickstream.health.CSHealthEvent>) {
        healthEventDao.deleteHealthEvent(events)
    }
}
