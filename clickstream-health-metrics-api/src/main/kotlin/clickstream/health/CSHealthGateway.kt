package clickstream.health

public interface CSHealthGateway {
    public val eventHealthListener: CSEventHealthListener

    public val healthEventRepository: CSHealthEventRepository

    public val healthEventProcessor: CSHealthEventProcessor

    public val healthEventFactory: CSHealthEventFactory
}