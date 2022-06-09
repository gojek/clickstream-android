package clickstream.interceptor

import clickstream.internal.eventscheduler.CSEventData
import com.google.protobuf.MessageLite

public open class InterceptedEvent(public vararg val events: CSEventData) {

    public class Instant(public val event: CSEventData, public val messageLite: MessageLite) :
        InterceptedEvent(event)

    public class Scheduled(public val event: CSEventData, public val messageLite: MessageLite) :
        InterceptedEvent(event)

    public class Dispatched(eventList: List<CSEventData>) :
        InterceptedEvent(*eventList.toTypedArray())

    public class Registered(eventList: List<CSEventData>) :
        InterceptedEvent(*eventList.toTypedArray())

}