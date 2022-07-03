package clickstream.fake

import clickstream.health.intermediate.CSHealthEventFactory
import com.gojek.clickstream.internal.Health

internal class FakeCSHealthEventFactory : CSHealthEventFactory {
    override suspend fun create(message: Health): Health {
        return message
    }
}