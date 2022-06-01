package clickstream.internal.networklayer

import clickstream.api.CSInfo
import clickstream.config.CSNetworkConfig
import clickstream.health.intermediate.CSHealthEventRepository
import clickstream.health.time.CSTimeStampGenerator
import clickstream.internal.utils.CSCallback
import clickstream.logger.CSLogger
import com.gojek.clickstream.de.EventRequest
import com.gojek.clickstream.de.common.EventResponse
import com.tinder.scarlet.WebSocket
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow

/**
 * The Network Repository communicates between service and the manager
 * It sends the events to the server as well as listens to the states of the connection
 */
internal interface CSNetworkRepository {

    /**
     * Observes the acknowledgement from the BE
     * @return Flow<> - Stream of event data containing the ID of successful events
     */
    public fun observeResponse(): Flow<EventResponse>

    /**
     * Observes the socket connection - Opened, closed, IsClosing, Failed, MessageReceived
     * @return Flow<WebSocket.Event> - Changes in socket connection is observed
     */
    public fun observeSocketState(): Flow<WebSocket.Event>

    /**
     * Sends the event to the BE
     * @param eventRequest - The analytic data which is to be sent
     */
    public fun sendEvents(
        eventRequest: EventRequest,
        callback: CSCallback<String>
    )

    /**
     * Sends the event to the BE instantly and doesn't require a callback
     * @param eventRequest - The analytic data which is to be sent
     */
    public fun sendInstantEvents(
        eventRequest: EventRequest
    )
}

/**
 * The NetworkRepositoryImpl is the implementation of NetworkRepository
 * @param eventService - Service which creates socket connection and handles the data
 */
internal class CSNetworkRepositoryImpl(
    private val networkConfig: CSNetworkConfig,
    private val eventService: CSEventService,
    private val dispatcher: CoroutineDispatcher,
    private val timeStampGenerator: CSTimeStampGenerator,
    private val logger: CSLogger,
    private val healthEventRepository: CSHealthEventRepository,
    private val info: CSInfo
) : CSNetworkRepository {

    override fun observeResponse(): Flow<EventResponse> {
        logger.debug { "CSNetworkRepositoryImpl#observeResponse" }

        return eventService.observeResponse()
    }

    override fun observeSocketState(): Flow<WebSocket.Event> {
        logger.debug { "CSNetworkRepositoryImpl#observeSocketState" }

        return eventService.observeSocketState()
    }

    override fun sendEvents(
        eventRequest: EventRequest,
        callback: CSCallback<String>
    ) {
        logger.debug { "CSNetworkRepositoryImpl#sendEvents" }

        object : CSRetryableCallback(
            networkConfig = networkConfig,
            eventService = eventService,
            eventRequest = eventRequest,
            dispatcher = dispatcher,
            timeStampGenerator = timeStampGenerator,
            logger = logger,
            healthEventRepository = healthEventRepository,
            info = info
        ) {
            override fun onSuccess(guid: String) {
                logger.debug { "CSNetworkRepositoryImpl#sendEvents#onSuccess - $guid" }

                callback.onSuccess(guid)
            }

            override fun onFailure(throwable: Throwable, guid: String) {
                logger.debug { "CSNetworkRepositoryImpl#sendEvents#onFailure - $guid ${throwable.stackTraceToString()}" }

                callback.onError(throwable, guid)
            }
        }
    }

    override fun sendInstantEvents(eventRequest: EventRequest) {
        logger.debug { "CSNetworkRepositoryImpl#sendInstantEvents - eventRequest $eventRequest" }
        logger.debug { "CSNetworkRepositoryImpl#sendInstantEvents - eventRequestType ${eventRequest.eventsList[0].type}" }

        val event = eventService.sendEvent(eventRequest)
        logger.debug { "CSNetworkRepositoryImpl#sendInstantEvents - eventRequest isSuccess $event" }
    }
}
