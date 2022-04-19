package clickstream.internal.networklayer

import com.gojek.clickstream.de.EventRequest
import com.gojek.clickstream.de.common.EventResponse
import com.tinder.scarlet.WebSocket
import com.tinder.scarlet.ws.Receive
import com.tinder.scarlet.ws.Send
import kotlinx.coroutines.flow.Flow

/**
 * The EventService contains the definition for the Scarlet to create a connection
 */
internal interface CSEventService {

    /**
     * Observes the acknowledgement from the BE
     * @return Flow<> - Stream of event data containing the ID of successful events
     */
    @Receive
    fun observeResponse(): Flow<EventResponse>

    /**
     * Observes the socket connection - Opened, closed, IsClosing, Failed, MessageReceived
     * @return Flow<WebSocket.Event> - Changes in socket connection is observed
     */
    @Receive
    fun observeSocketState(): Flow<WebSocket.Event>

    /**
     * Sends the event to the BE
     * @param streamBatchEvents - The analytic data which is to be sent
     */
    @Send
    fun sendEvent(streamBatchEvents: EventRequest): Boolean
}
