package clickstream.health.internal

import clickstream.health.intermediate.CSHealthEventProcessor
import com.gojek.clickstream.internal.Health

internal class NoOpCSHealthEventProcessor : CSHealthEventProcessor {
    override suspend fun getAggregateEventsBasedOnEventName(): List<Health> {
        return emptyList()
    }
}
