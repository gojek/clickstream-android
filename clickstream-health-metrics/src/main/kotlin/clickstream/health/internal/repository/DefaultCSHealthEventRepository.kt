package clickstream.health.internal.repository

import clickstream.api.CSInfo
import clickstream.health.constant.CSEventTypesConstant
import clickstream.health.internal.database.CSHealthEventEntity.Companion.dtoMapTo
import clickstream.health.internal.database.CSHealthEventEntity.Companion.mapToDto
import clickstream.health.internal.database.CSHealthEventDao
import clickstream.health.model.CSHealthEvent
import kotlinx.coroutines.ExperimentalCoroutinesApi

/**
 * The HealthRepositoryImpl is the implementation detail of the [CSHealthEventRepository].
 *
 * @param healthEventDao - The Dao object to communicate to the DB
 */
@ExperimentalCoroutinesApi
internal class DefaultCSHealthEventRepository(
    private val healthEventDao: CSHealthEventDao,
    private val info: CSInfo
) : CSHealthEventRepository {

    override suspend fun insertHealthEvent(healthEvent: CSHealthEvent) {
        val event = healthEvent.copy(
            sessionId = info.sessionInfo.sessionID,
            timestamp = System.currentTimeMillis().toString(),
            appVersion = info.appInfo.appVersion
        )
        healthEventDao.insert(healthEvent = event.dtoMapTo())
    }

    override suspend fun insertHealthEventList(healthEventList: List<CSHealthEvent>) {
        val eventList = healthEventList.map {
            it.copy(
                sessionId = info.sessionInfo.sessionID,
                timestamp = System.currentTimeMillis().toString()
            )
        }.toList()
        healthEventDao.insertAll(healthEventList = eventList.map { it.dtoMapTo() })
    }

    override suspend fun getInstantEvents(size: Int): List<CSHealthEvent> {
        return healthEventDao.getEventByType(CSEventTypesConstant.INSTANT, size)
            .map { it.mapToDto() }
    }

    override suspend fun getAggregateEvents(size: Int): List<CSHealthEvent> {
        return healthEventDao.getEventByType(CSEventTypesConstant.AGGREGATE, size)
            .map { it.mapToDto() }
    }

    override suspend fun deleteHealthEventsBySessionId(sessionId: String) {
        healthEventDao.deleteBySessionId(sessionId = sessionId)
    }

    override suspend fun deleteHealthEvents(events: List<CSHealthEvent>) {
        healthEventDao.deleteHealthEvent(events.map { it.dtoMapTo() })
    }

    override suspend fun deleteHealthEventsByType(type: String) {
        healthEventDao.deleteHealthEventByType(type)
    }

    override suspend fun getEventCount(type: String): Int {
        return healthEventDao.getHealthEventCountByType(type)
    }

    override suspend fun getEventsByTypeAndLimit(type: String, limit: Int): List<CSHealthEvent> {
        return healthEventDao.getEventByType(type, limit).map { it.mapToDto() }
    }
}
