package clickstream.internal.analytics.impl

import clickstream.CSInfo
import clickstream.internal.analytics.AGGREGATE_EVENT_TYPE
import clickstream.internal.analytics.BUCKET_EVENT_TYPE
import clickstream.internal.analytics.CSHealthEvent
import clickstream.internal.analytics.CSHealthEventDao
import clickstream.internal.analytics.CSHealthEventRepository
import clickstream.internal.analytics.INSTANT_EVENT_TYPE

/**
 * The HealthRepositoryImpl is the implementation detail of the [CSHealthEventRepository].
 *
 * @param healthEventDao - The Dao object to communicate to the DB
 */
internal class DefaultCSHealthEventRepository(
    private val sessionId: String,
    private val healthEventDao: CSHealthEventDao,
    private val info: CSInfo
) : CSHealthEventRepository {

    override suspend fun insertHealthEvent(healthEvent: CSHealthEvent) {
        val event = healthEvent.copy(
            sessionId = sessionId,
            timestamp = System.currentTimeMillis().toString(),
            appVersion = info.appInfo.appVersion
        )
        healthEventDao.insert(healthEvent = event)
    }

    override suspend fun insertHealthEventList(healthEventList: List<CSHealthEvent>) {
        val eventList = healthEventList.map {
            it.copy(
                sessionId = sessionId,
                timestamp = System.currentTimeMillis().toString()
            )
        }.toList()
        healthEventDao.insertAll(healthEventList = eventList)
    }

    override suspend fun getInstantEvents(): List<CSHealthEvent> {
        return healthEventDao.getEventByType(INSTANT_EVENT_TYPE)
    }

    override suspend fun getBucketEvents(): List<CSHealthEvent> {
        return healthEventDao.getEventByType(BUCKET_EVENT_TYPE)
    }

    override suspend fun getAggregateEvents(): List<CSHealthEvent> {
        return healthEventDao.getEventByType(AGGREGATE_EVENT_TYPE)
    }

    override suspend fun deleteHealthEventsBySessionId(sessionId: String) {
        healthEventDao.deleteBySessionId(sessionId = sessionId)
    }

    override suspend fun deleteHealthEvents(events: List<CSHealthEvent>) {
        healthEventDao.deleteHealthEvent(events)
    }
}
