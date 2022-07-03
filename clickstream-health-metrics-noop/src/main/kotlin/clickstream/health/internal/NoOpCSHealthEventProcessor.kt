package clickstream.health.internal

import clickstream.health.intermediate.CSHealthEventProcessor
import com.gojek.clickstream.internal.Health

internal class NoOpCSHealthEventProcessor : CSHealthEventProcessor {
    override suspend fun getAggregateEvents(): List<Health> {
        return emptyList()
    }

    override suspend fun getInstantEvents(): List<Health> {
        return emptyList()
    }
}
