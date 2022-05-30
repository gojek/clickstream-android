package clickstream.health

import com.gojek.clickstream.internal.Health

/**
 * This is responsible for creating an event object in proto format when provided with user-generated,
 * common data, event-specific attributes, and priority.
 */
public interface CSHealthEventFactory {

    /**
     * Creates Domain-Specific Model (ClickSteam Domain) which holds event data with a given priority.
     *
     * @param message genric protobuf message which is wrapped within the CSEventMessage
     * @return Event Object
     */
    public suspend fun create(message: Health): Health
}
