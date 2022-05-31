package clickstream.fake

import clickstream.health.CSHealthGateway
import clickstream.health.intermediate.CSEventHealthListener
import clickstream.health.intermediate.CSHealthEventFactory
import clickstream.health.intermediate.CSHealthEventProcessor
import clickstream.health.intermediate.CSHealthEventRepository

internal class FakeHealthGateway(
    override val eventHealthListener: CSEventHealthListener,
    override val healthEventRepository: CSHealthEventRepository,
    override val healthEventProcessor: CSHealthEventProcessor,
    override val healthEventFactory: CSHealthEventFactory,
) : CSHealthGateway