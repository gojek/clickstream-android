package clickstream.health.internal

import clickstream.health.intermediate.CSHealthEventFactory
import clickstream.health.proto.Health

internal class NoOpCSHealthEventFactory : CSHealthEventFactory {
    override suspend fun create(message: Health): Health {
        throw IllegalAccessException("Not allowed")
    }
}