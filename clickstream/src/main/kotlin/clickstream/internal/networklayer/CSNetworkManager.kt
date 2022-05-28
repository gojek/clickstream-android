package clickstream.internal.networklayer

import clickstream.CSInfo
import clickstream.connection.CSConnectionEvent.OnConnectionClosed
import clickstream.connection.CSConnectionEvent.OnConnectionClosing
import clickstream.connection.CSConnectionEvent.OnConnectionConnected
import clickstream.connection.CSConnectionEvent.OnConnectionConnecting
import clickstream.connection.CSConnectionEvent.OnConnectionFailed
import clickstream.connection.CSConnectionEvent.OnMessageReceived
import clickstream.connection.CSSocketConnectionListener
import clickstream.connection.mapTo
import clickstream.internal.analytics.CSErrorReasons
import clickstream.internal.analytics.CSEventNames
import clickstream.internal.analytics.CSHealthEvent
import clickstream.internal.analytics.CSHealthEventRepository
import clickstream.internal.analytics.EventTypes
import clickstream.internal.lifecycle.CSAppLifeCycle
import clickstream.internal.lifecycle.CSLifeCycleManager
import clickstream.internal.utils.CSResult
import clickstream.logger.CSLogger
import com.gojek.clickstream.de.EventRequest
import com.tinder.scarlet.WebSocket
import java.net.SocketTimeoutException
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * The NetworkManager is responsible for communicating with repository and the event scheduler
 *
 * @param networkRepository - Handles the communication with the server
 * @param dispatcher - CoroutineDispatcher on which the events are observed and processed
 * @param logger - To create logs
 * @param healthEventRepository - Used for logging health events
 */
@ExperimentalCoroutinesApi
internal open class CSNetworkManager(
    appLifeCycleObserver: CSAppLifeCycle,
    private val networkRepository: CSNetworkRepository,
    protected val dispatcher: CoroutineDispatcher,
    protected val logger: CSLogger,
    private val healthEventRepository: CSHealthEventRepository,
    private val info: CSInfo,
    private val connectionListener: CSSocketConnectionListener
) : CSLifeCycleManager(appLifeCycleObserver) {

    private val isConnected: AtomicBoolean = AtomicBoolean(false)

    protected var job: CompletableJob = SupervisorJob()
    protected var coroutineScope: CoroutineScope = CoroutineScope(job + dispatcher)
    private val handler = CoroutineExceptionHandler { _, throwable ->
        logger.debug { "CSNetworkManager#handler : ${throwable.message}" }
    }

    init {
        logger.debug { "CSNetworkManager#init" }
        addObserver()
    }

    /**
     * Provides status of network manager
     *
     * @return isAvailable - Whether network manager is available or not
     */
    fun isAvailable(): Boolean {
        logger.debug { "CSNetworkManager#isAvailable - isSocketConnected : ${isConnected.get()}" }

        return isConnected.get()
    }

    /**
     * Updates the caller with the status of the request
     */
    private lateinit var callback: CSEventGuidCallback

    val eventGuidFlow: Flow<CSResult<String>> = callbackFlow {
        logger.debug { "CSNetworkManager#eventGuidFlow" }

        callback = object : CSEventGuidCallback {
            override fun onSuccess(data: String) {
                logger.debug { "CSNetworkManager#eventGuidFlow#onSuccess - $data" }

                offer(CSResult.Success(data))
            }

            override fun onError(error: Throwable, guid: String) {
                error.printStackTrace()
                logger.debug { "CSNetworkManager#eventGuidFlow#onError - $guid errorMessage : ${error.message}" }

                offer(CSResult.Failure(error, guid))
            }
        }
        awaitClose()
    }

    /**
     * Starts the network manager by creating the coroutine scope
     */
    override fun onStart() {
        logger.debug { "CSNetworkManager#onStart" }
        job = SupervisorJob()
        coroutineScope = CoroutineScope(job + dispatcher)
        observeSocketState()
    }

    /**
     * Terminates the network manager by cancelling the coroutine, will be called when
     * clickstream is getting terminated.
     */
    override fun onStop() {
        logger.debug { "CSNetworkManager#onStop" }
        coroutineScope.cancel()
    }

    /**
     * The analytic data which is sent to the server
     *
     * @param eventRequest - The data which hold the analytic events
     */
    fun processEvent(
        eventRequest: EventRequest
    ) {
        logger.debug { "CSNetworkManager#processEvent" }
        networkRepository.sendEvents(
            eventRequest = eventRequest,
            callback = callback
        )
    }

    /**
     * The instant/health data which is sent to the server without
     *
     * @param eventRequest - The data which hold the analytic events
     */
    fun processInstantEvent(
        eventRequest: EventRequest
    ) {
        logger.debug { "CSNetworkManager#processInstantEvent" }
        networkRepository.sendInstantEvents(eventRequest = eventRequest)
    }

    /**
     * Observes the web socket connection state.
     *
     * When the connection is closed, the scope is cancelled to unsubscribe the events
     */
    protected fun observeSocketState(): Job {
        logger.debug { "CSNetworkManager#observeSocketState" }

        return coroutineScope.launch(handler) {
            logger.debug { "CSNetworkManager#observeSocketState is coroutine active : $isActive" }

            ensureActive()
            networkRepository.observeSocketState()
                .onStart {
                    connectionListener.onEventChanged(OnConnectionConnecting)
                }
                .collect {
                    when (it) {
                        is WebSocket.Event.OnConnectionOpened<*> -> {
                            connectionListener.onEventChanged(OnConnectionConnected)
                            logger.debug { "CSNetworkManager#observeSocketState - Socket State: OnConnectionOpened" }
                            isConnected.set(true)
                        }
                        is WebSocket.Event.OnMessageReceived -> {
                            connectionListener.onEventChanged(OnMessageReceived(it.message.mapTo()))
                            logger.debug { "CSNetworkManager#observeSocketState - Socket State: OnMessageReceived : " + it.message }
                        }
                        is WebSocket.Event.OnConnectionClosing -> {
                            connectionListener.onEventChanged(OnConnectionClosing(it.shutdownReason.mapTo()))
                            logger.debug { "CSNetworkManager#observeSocketState - Socket State: OnConnectionClosing. Due to" + it.shutdownReason }
                        }
                        is WebSocket.Event.OnConnectionClosed -> {
                            connectionListener.onEventChanged(OnConnectionClosed(it.shutdownReason.mapTo()))
                            logger.debug { "CSNetworkManager#observeSocketState - Socket State: OnConnectionClosed. Due to" + it.shutdownReason }
                            isConnected.set(false)
                        }
                        is WebSocket.Event.OnConnectionFailed -> {
                            connectionListener.onEventChanged(OnConnectionFailed(it.throwable))
                            logger.debug { "CSNetworkManager#observeSocketState - Socket State: OnConnectionFailed. Due to " + it.throwable }
                            isConnected.set(false)
                            trackConnectionFailure(it)
                        }
                    }
                }
        }
    }

    private suspend fun trackConnectionFailure(failureResponse: WebSocket.Event.OnConnectionFailed) {
        logger.debug { "CSNetworkManager#trackConnectionFailure $failureResponse" }

        val healthEvent = when {
            failureResponse.throwable.message?.contains(CSErrorReasons.USER_UNAUTHORIZED, true)
                ?: false -> {
                CSHealthEvent(
                    eventName = CSEventNames.ClickStreamConnectionFailed.value,
                    eventType = EventTypes.AGGREGATE,
                    error = CSErrorReasons.USER_UNAUTHORIZED,
                    appVersion = info.appInfo.appVersion
                )
            }
            failureResponse.throwable is SocketTimeoutException -> {
                CSHealthEvent(
                    eventName = CSEventNames.ClickStreamConnectionFailed.value,
                    eventType = EventTypes.AGGREGATE,
                    error = CSErrorReasons.SOCKET_TIMEOUT,
                    appVersion = info.appInfo.appVersion
                )
            }
            failureResponse.throwable.message?.isNotEmpty() ?: false -> {
                CSHealthEvent(
                    eventName = CSEventNames.ClickStreamConnectionFailed.value,
                    eventType = EventTypes.AGGREGATE,
                    error = failureResponse.throwable.toString(),
                    appVersion = info.appInfo.appVersion
                )
            }
            else -> {
                CSHealthEvent(
                    eventName = CSEventNames.ClickStreamConnectionFailed.value,
                    eventType = EventTypes.AGGREGATE,
                    error = CSErrorReasons.UNKNOWN,
                    appVersion = info.appInfo.appVersion
                )
            }
        }
        healthEventRepository.insertHealthEvent(healthEvent = healthEvent)
    }
}
