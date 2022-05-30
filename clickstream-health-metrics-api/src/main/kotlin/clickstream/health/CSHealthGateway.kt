package clickstream.health

import clickstream.lifecycle.CSAppLifeCycle

public interface CSHealthGateway {
    public val appLifeCycle: CSAppLifeCycle

    public val eventHealthListener: CSEventHealthListener

    public val healthEventRepository: CSHealthEventRepository

    public val healthEventProcessor: CSHealthEventProcessor

    public val healthEventFactory: CSHealthEventFactory
}