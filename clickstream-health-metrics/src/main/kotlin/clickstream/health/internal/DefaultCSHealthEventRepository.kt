package clickstream.health.internal

import clickstream.health.model.CSHealthEventDTO
import clickstream.health.CSHealthEventRepository
import clickstream.health.CSInfo
import clickstream.health.internal.CSHealthEvent.Companion.dtoMapTo
import clickstream.health.internal.CSHealthEvent.Companion.dtosMapTo
import clickstream.health.internal.CSHealthEvent.Companion.mapToDtos

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

    override suspend fun insertHealthEvent(healthEvent: CSHealthEventDTO) {
        val event = healthEvent.dtoMapTo()
            .copy(
                sessionId = sessionId,
                timestamp = System.currentTimeMillis().toString(),
                appVersion = info.appInfo.appVersion
            )
        healthEventDao.insert(healthEvent = event)
    }

    override suspend fun insertHealthEventList(healthEventList: List<CSHealthEventDTO>) {
        val eventList: List<CSHealthEvent> = healthEventList.map { eventDto ->
            eventDto.dtoMapTo()
                .copy(
                    sessionId = sessionId,
                    timestamp = System.currentTimeMillis().toString()
                )
        }.toList()
        healthEventDao.insertAll(healthEventList = eventList)
    }

    override suspend fun getInstantEvents(): List<CSHealthEventDTO> {
        return healthEventDao.getEventByType(INSTANT_EVENT_TYPE).mapToDtos()
    }

    override suspend fun getBucketEvents(): List<CSHealthEventDTO> {
        return healthEventDao.getEventByType(BUCKET_EVENT_TYPE).mapToDtos()
    }

    override suspend fun getAggregateEvents(): List<CSHealthEventDTO> {
        return healthEventDao.getEventByType(AGGREGATE_EVENT_TYPE).mapToDtos()
    }

    override suspend fun deleteHealthEventsBySessionId(sessionId: String) {
        healthEventDao.deleteBySessionId(sessionId = sessionId)
    }

    override suspend fun deleteHealthEvents(events: List<CSHealthEventDTO>) {
        healthEventDao.deleteHealthEvent(events.dtosMapTo())
    }
}
