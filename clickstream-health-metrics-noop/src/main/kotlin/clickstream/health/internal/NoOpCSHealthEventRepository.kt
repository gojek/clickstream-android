package clickstream.health.internal

import clickstream.health.intermediate.CSHealthEventRepository
import clickstream.health.model.CSHealthEvent
import clickstream.health.model.CSHealthEventDTO

internal class NoOpCSHealthEventRepository : CSHealthEventRepository {
    override suspend fun insertHealthEvent(healthEvent: CSHealthEventDTO) {
        /*NoOp*/
    }

    override suspend fun insertHealthEventList(healthEventList: List<CSHealthEventDTO>) {
        /*NoOp*/
    }

    override suspend fun getInstantEvents(): List<CSHealthEventDTO> {
        return emptyList()
    }

    override suspend fun getBucketEvents(): List<CSHealthEventDTO> {
        return emptyList()
    }

    override suspend fun getAggregateEvents(): List<CSHealthEventDTO> {
        return emptyList()
    }

    override suspend fun deleteHealthEventsBySessionId(sessionId: String) {
        /*NoOp*/
    }

    override suspend fun deleteHealthEvents(events: List<CSHealthEventDTO>) {
        /*NoOp*/
    }
}