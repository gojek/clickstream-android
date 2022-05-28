package clickstream.internal.analytics.impl

import clickstream.CSInfo
import clickstream.config.CSRemoteConfig
import clickstream.internal.analytics.AGGREGATE_EVENT_TYPE
import clickstream.internal.analytics.BUCKET_EVENT_TYPE
import clickstream.internal.analytics.CSHealthEvent
import clickstream.internal.analytics.CSHealthEventDao
import clickstream.internal.analytics.CSHealthEventRepository
import clickstream.internal.analytics.INSTANT_EVENT_TYPE
import clickstream.logger.CSLogger

/**
 * The HealthRepositoryImpl is the implementation detail of the [CSHealthEventRepository].
 *
 * @param healthEventDao - The Dao object to communicate to the DB
 */
internal class DefaultCSHealthEventRepository(
    private val sessionId: String,
    private val healthEventDao: CSHealthEventDao,
    private val info: CSInfo,
    private val remoteConfig: CSRemoteConfig,
    private val logger: CSLogger
) : CSHealthEventRepository {

    override suspend fun insertHealthEvent(healthEvent: CSHealthEvent) {
        if (printMessage { "insertHealthEvent" }) return

        val event = healthEvent.copy(
            sessionId = sessionId,
            timestamp = System.currentTimeMillis().toString(),
            appVersion = info.appInfo.appVersion
        )
        healthEventDao.insert(healthEvent = event)
    }

    override suspend fun insertHealthEventList(healthEventList: List<CSHealthEvent>) {
        if (printMessage { "insertHealthEventList" }) return

        val eventList = healthEventList.map {
            it.copy(
                sessionId = sessionId,
                timestamp = System.currentTimeMillis().toString()
            )
        }.toList()
        healthEventDao.insertAll(healthEventList = eventList)
    }

    override suspend fun getInstantEvents(): List<CSHealthEvent> {
        if (printMessage { "getInstantEvents" }) return emptyList()

        return healthEventDao.getEventByType(INSTANT_EVENT_TYPE)
    }

    override suspend fun getBucketEvents(): List<CSHealthEvent> {
        if (printMessage { "getBucketEvents" }) return emptyList()

        return healthEventDao.getEventByType(BUCKET_EVENT_TYPE)
    }

    override suspend fun getAggregateEvents(): List<CSHealthEvent> {
        if (printMessage { "getAggregateEvents" }) return emptyList()

        return healthEventDao.getEventByType(AGGREGATE_EVENT_TYPE)
    }

    override suspend fun deleteHealthEventsBySessionId(sessionId: String) {
        if (printMessage { "deleteHealthEventsBySessionId" }) return

        healthEventDao.deleteBySessionId(sessionId = sessionId)
    }

    override suspend fun deleteHealthEvents(events: List<CSHealthEvent>) {
        if (printMessage { "deleteHealthEvents" }) return

        healthEventDao.deleteHealthEvent(events)
    }

    private fun printMessage(function: () -> String): Boolean {
        if (remoteConfig.isHealthMetricsEnabled.not()) {
            logger.debug {
                "HealthMetrics is disabled, ${function()} is ignored"
            }
            return true
        }
        return false
    }
}
