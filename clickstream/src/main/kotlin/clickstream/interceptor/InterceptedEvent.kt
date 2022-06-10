package clickstream.interceptor

import clickstream.internal.eventscheduler.CSEventData
import com.google.protobuf.MessageLite

/**
 * Wrapper class around [CSEventData] representing intercepted event for [EventInterceptor.onIntercept].
 *
 * Instant events are represented by [Instant] state.
 * Scheduled events have [Scheduled] -> [Dispatched] -> [Acknowledged] states.
 *
 * @property events
 */
public abstract class InterceptedEvent(public vararg val events: CSEventData) {

    /**
     * Represents fire and forget events.
     *
     * @property event
     * @property messageLite
     */
    public class Instant(
        public val event: CSEventData,
        public val messageLite: MessageLite
    ) : InterceptedEvent(event)

    /**
     * Represents events that are currently scheduled to be dispatched.
     *
     * @property event
     * @property messageLite
     */
    public class Scheduled(
        public val event: CSEventData,
        public val messageLite: MessageLite
    ) : InterceptedEvent(event)

    /**
     * Represents events that are dispatched.
     *
     * @param eventList
     */
    public class Dispatched(eventList: List<CSEventData>) :
        InterceptedEvent(*eventList.toTypedArray())

    /**
     * Represents events that are dispatched and acknowledged by the server.
     *
     * @param eventList
     */
    public class Acknowledged(eventList: List<CSEventData>) :
        InterceptedEvent(*eventList.toTypedArray())

}