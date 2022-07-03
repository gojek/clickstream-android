package clickstream.fake

import clickstream.health.intermediate.CSHealthEventRepository
import clickstream.health.model.CSHealthEventDTO

internal class FakeCSHealthEventRepository(
    private val fakeHealthEvents: List<CSHealthEventDTO>
) : CSHealthEventRepository {

    private val stubbedHealthEvents = mutableListOf<CSHealthEventDTO>()
        .apply {
            addAll(fakeHealthEvents)
        }

    override suspend fun insertHealthEvent(healthEvent: CSHealthEventDTO) {
        stubbedHealthEvents.add(healthEvent)
    }

    override suspend fun insertHealthEventList(healthEventList: List<CSHealthEventDTO>) {
        stubbedHealthEvents.addAll(healthEventList)
    }

    override suspend fun getInstantEvents(): List<CSHealthEventDTO> {
        return stubbedHealthEvents
    }

    override suspend fun getAggregateEvents(): List<CSHealthEventDTO> {
        return stubbedHealthEvents
    }

    override suspend fun deleteHealthEventsBySessionId(sessionId: String) {
        val events = stubbedHealthEvents.filter { it.sessionId == sessionId }
        stubbedHealthEvents.removeAll(events)
    }

    override suspend fun deleteHealthEvents(events: List<CSHealthEventDTO>) {
        stubbedHealthEvents.removeAll(events)
    }
}
