package clickstream.internal.eventprocessor

import com.gojek.clickstream.internal.Health

/**
 * This is responsible for creating an event object in proto format when provided with user-generated,
 * common data, event-specific attributes, and priority.
 */
internal interface CSHealthEventFactory {

    /**
     * Creates Domain-Specific Model (ClickSteam Domain) which holds event data with a given priority.
     *
     * @param message genric protobuf message which is wrapped within the CSEventMessage
     * @return Event Object
     */
    suspend fun create(message: Health): Health
}
