package clickstream.health

import clickstream.health.intermediate.CSEventHealthListener
import clickstream.health.intermediate.CSHealthEventFactory
import clickstream.health.intermediate.CSHealthEventProcessor
import clickstream.health.intermediate.CSHealthEventRepository
import clickstream.lifecycle.CSAppLifeCycle

public interface CSHealthGateway {
    public val appLifeCycle: CSAppLifeCycle

    public val eventHealthListener: CSEventHealthListener

    public val healthEventRepository: CSHealthEventRepository

    public val healthEventProcessor: CSHealthEventProcessor

    public val healthEventFactory: CSHealthEventFactory
}