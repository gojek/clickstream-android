package clickstream.health

import clickstream.health.intermediate.CSEventHealthListener
import clickstream.health.intermediate.CSHealthEventFactory
import clickstream.health.intermediate.CSHealthEventProcessor
import clickstream.health.intermediate.CSHealthEventRepository
import clickstream.health.internal.NoOpCSEventHealthListener
import clickstream.health.internal.NoOpCSHealthEventFactory
import clickstream.health.internal.NoOpCSHealthEventProcessor
import clickstream.health.internal.NoOpCSHealthEventRepository

public object NoOpCSHealthGateway {

    public fun factory(): CSHealthGateway {
        return object : CSHealthGateway {
            override val eventHealthListener: CSEventHealthListener by lazy {
                NoOpCSEventHealthListener()
            }
            override val healthEventRepository: CSHealthEventRepository by lazy {
                NoOpCSHealthEventRepository()
            }
            override val healthEventProcessor: CSHealthEventProcessor by lazy {
                NoOpCSHealthEventProcessor()
            }
            override val healthEventFactory: CSHealthEventFactory by lazy {
                NoOpCSHealthEventFactory()
            }
        }
    }
}
