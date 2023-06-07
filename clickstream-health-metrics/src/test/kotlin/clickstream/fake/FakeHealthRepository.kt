package clickstream.fake

import clickstream.health.constant.CSEventTypesConstant
import clickstream.health.internal.repository.CSHealthEventRepository
import clickstream.health.model.CSHealthEvent

internal class FakeHealthRepository : CSHealthEventRepository {

    private val healthList = mutableListOf<CSHealthEvent>()

    override suspend fun insertHealthEvent(healthEvent: CSHealthEvent) {
        healthList.add(healthEvent)
    }

    override suspend fun insertHealthEventList(healthEventList: List<CSHealthEvent>) {
        healthList.addAll(healthEventList)
    }

    override suspend fun getInstantEvents(size: Int): List<CSHealthEvent> {
        return healthList.filter { it.eventType == CSEventTypesConstant.INSTANT }
    }

    override suspend fun getAggregateEvents(size: Int): List<CSHealthEvent> {
        return healthList.filter { it.eventType == CSEventTypesConstant.AGGREGATE }
    }

    override suspend fun deleteHealthEventsBySessionId(sessionId: String) {
        /*NoOp*/
    }

    override suspend fun deleteHealthEvents(events: List<CSHealthEvent>) {
        healthList.removeAll(events)
    }

    override suspend fun deleteHealthEventsByType(type: String) {
        healthList.removeIf { it.eventType == type }
    }

    override suspend fun getEventCount(type: String): Int {
        return healthList.size
    }

    override suspend fun getEventsByTypeAndLimit(type: String, limit: Int): List<CSHealthEvent> {
        return healthList.filter { it.eventType == type }
    }
}