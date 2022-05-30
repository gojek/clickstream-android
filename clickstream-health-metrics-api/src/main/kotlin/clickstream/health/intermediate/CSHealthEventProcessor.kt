package clickstream.health.intermediate

import com.gojek.clickstream.internal.Health

public interface CSHealthEventProcessor {
    public suspend fun getAggregateEventsBasedOnEventName(): List<Health>
}