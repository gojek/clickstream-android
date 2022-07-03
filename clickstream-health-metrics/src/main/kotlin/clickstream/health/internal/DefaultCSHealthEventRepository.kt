package clickstream.health.internal

import androidx.annotation.RestrictTo
import clickstream.api.CSInfo
import clickstream.health.intermediate.CSHealthEventRepository
import clickstream.health.internal.CSHealthEventEntity.Companion.dtoMapTo
import clickstream.health.internal.CSHealthEventEntity.Companion.dtosMapTo
import clickstream.health.internal.CSHealthEventEntity.Companion.mapToDtos
import clickstream.health.model.CSHealthEventDTO

/**
 * [CSHealthEventRepository] Act as repository pattern where internally it doing DAO operation
 * to insert, delete, and read the [CSHealthEventEntity]'s.
 *
 * If you're using `com.gojek.clickstream:clickstream-health-metrics-noop`, the
 * [CSHealthEventRepository] internally will doing nothing.
 *
 * Do consider to use `com.gojek.clickstream:clickstream-health-metrics`, to operate
 * [CSHealthEventRepository] as expected. Whenever you opt in the `com.gojek.clickstream:clickstream-health-metrics`,
 * you should never touch the [DefaultCSHealthEventRepository] explicitly. All the wiring
 * is happening through [DefaultCSHealthGateway.factory(/*args*/)]
 *
 * @param sessionId in form of UUID
 * @param healthEventDao the [CSHealthEventDao]
 * @param info the [CSInfo]
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
internal class DefaultCSHealthEventRepository(
    private val sessionId: String,
    private val healthEventDao: CSHealthEventDao,
    private val info: CSInfo
) : CSHealthEventRepository {

    override suspend fun insertHealthEvent(healthEvent: CSHealthEventDTO) {
        val event: CSHealthEventEntity = healthEvent.dtoMapTo()
            .copy(
                sessionId = sessionId,
                timestamp = System.currentTimeMillis().toString(),
                appVersion = info.appInfo.appVersion
            )
        healthEventDao.insert(healthEvent = event)
    }

    override suspend fun insertHealthEventList(healthEventList: List<CSHealthEventDTO>) {
        val eventList: List<CSHealthEventEntity> = healthEventList.map { eventDto ->
            eventDto.dtoMapTo()
                .copy(
                    sessionId = sessionId,
                    timestamp = System.currentTimeMillis().toString(),
                    appVersion = info.appInfo.appVersion
                )
        }.toList()
        healthEventDao.insertAll(healthEventList = eventList)
    }

    override suspend fun getInstantEvents(): List<CSHealthEventDTO> {
        return healthEventDao.getEventByType(INSTANT_EVENT_TYPE).mapToDtos()
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
