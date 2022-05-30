package clickstream.health.internal

import clickstream.health.intermediate.CSHealthEventFactory
import com.gojek.clickstream.internal.Health

internal class NoOpCSHealthEventFactory : CSHealthEventFactory {
    override suspend fun create(message: Health): Health {
        throw IllegalAccessException("Not allowed")
    }
}