package clickstream.internal.networklayer

import androidx.annotation.VisibleForTesting
import clickstream.api.CSInfo
import clickstream.connection.CSConnectionEvent.OnConnectionClosed
import clickstream.connection.CSConnectionEvent.OnConnectionClosing
import clickstream.connection.CSConnectionEvent.OnConnectionConnected
import clickstream.connection.CSConnectionEvent.OnConnectionConnecting
import clickstream.connection.CSConnectionEvent.OnConnectionFailed
import clickstream.connection.CSConnectionEvent.OnMessageReceived
import clickstream.connection.CSSocketConnectionListener
import clickstream.connection.mapTo
import clickstream.health.constant.CSEventNamesConstant
import clickstream.health.constant.CSEventTypesConstant
import clickstream.health.intermediate.CSHealthEventRepository
import clickstream.health.model.CSHealthEventDTO
import clickstream.internal.analytics.CSErrorReasons
import clickstream.internal.networklayer.proto.raccoon.SendEventRequest
import clickstream.internal.utils.CSResult
import clickstream.lifecycle.CSAppLifeCycle
import clickstream.lifecycle.CSLifeCycleManager
import clickstream.logger.CSLogger
import com.tinder.scarlet.WebSocket
import java.io.EOFException
import java.net.SocketTimeoutException
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.coroutines.coroutineContext

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
    appLifeCycle: CSAppLifeCycle,
    private val networkRepository: CSNetworkRepository,
    private val dispatcher: CoroutineDispatcher,
    private val logger: CSLogger,
    private val healthEventRepository: CSHealthEventRepository,
    private val info: CSInfo,
    private val connectionListener: CSSocketConnectionListener
) : CSLifeCycleManager(appLifeCycle) {

    private val isConnected: AtomicBoolean = AtomicBoolean(false)
    private val job = SupervisorJob()
    protected val coroutineScope = CoroutineScope(job + dispatcher)
    @VisibleForTesting
    internal var startConnectingTime: Long = 0L
    @VisibleForTesting
    internal var endConnectedTime: Long = 0L
    @Volatile
    private lateinit var callback: CSEventGuidCallback
    private val coroutineExceptionHandler: CoroutineExceptionHandler by lazy {
        CoroutineExceptionHandler { _, throwable ->
            logger.error {
                "================== CRASH IS HAPPENING ================== \n" +
                "= In : CSBackgroundEventScheduler                      = \n" +
                "= Due : ${throwable.message}                           = \n" +
                "==================== END OF CRASH ====================== \n"
            }
        }
    }

    init {
        logger.debug { "CSNetworkManager#init" }
        addObserver()
    }

    val eventGuidFlow: Flow<CSResult<String>> = callbackFlow {
        logger.debug { "CSNetworkManager#eventGuidFlow" }

        callback = object : CSEventGuidCallback {
            override fun onSuccess(data: String) {
                logger.debug { "CSNetworkManager#eventGuidFlow#onSuccess - $data" }

                offer(CSResult.Success(data))
            }

            override fun onError(error: Throwable, guid: String) {
                error.printStackTrace()
                logger.debug { "CSNetworkManager#eventGuidFlow#onError : $guid errorMessage ${error.message}" }

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
        coroutineScope.launch(coroutineExceptionHandler) {
            observeSocketConnectionState()
        }
    }

    /**
     * Terminates the network manager by cancelling the coroutine, will be called when
     * clickstream is getting terminated.
     */
    override fun onStop() {
        logger.debug { "CSNetworkManager#onStop" }
        cancelJob()
    }

    fun cancelJob() {
        logger.debug { "CSNetworkManager#cancelJob" }
        job.cancelChildren()
    }

    /**
     * The analytic data which is sent to the server
     *
     * @param eventRequest - The data which hold the analytic events
     * @param eventGuids - a guid list within string the comma separate "1, 2, 3"
     */
    fun processEvent(eventRequest: SendEventRequest, eventGuids: String) {
        logger.debug { "CSNetworkManager#processEvent" }

        networkRepository.sendEvents(eventRequest, eventGuids, callback)
    }

    /**
     * The instant/health data which is sent to the server without
     *
     * @param eventRequest - The data which hold the analytic events
     */
    fun processInstantEvent(
        eventRequest: SendEventRequest
    ) {
        logger.debug { "CSNetworkManager#processInstantEvent" }
        networkRepository.sendInstantEvents(eventRequest = eventRequest)
    }

    /**
     * Provides status of network manager
     *
     * @return isAvailable - Whether network manager is available or not
     */
    fun isSocketConnected(): Boolean {
        logger.debug { "CSNetworkManager#isSocketConnected : ${isConnected.get()}" }

        return this.isConnected.get()
    }

    /**
     * Observes the web socket connection state.
     *
     * When the connection is closed, the scope is cancelled to unsubscribe the events
     */
    @VisibleForTesting
    suspend fun observeSocketConnectionState() {
        logger.debug { "CSNetworkManager#observeSocketConnectionState" }

        if (coroutineContext.isActive.not()) {
            logger.debug { "CSNetworkManager#observeSocketConnectionState : coroutine is not active" }
            return
        }

        networkRepository.observeSocketState()
            .onStart {
                // start time for connecting
                startConnectingTime = System.currentTimeMillis()

                connectionListener.onEventChanged(OnConnectionConnecting)
                recordNonErrorHealthEvent(
                    eventName = CSEventNamesConstant.Instant.ClickStreamConnectionAttempt.value,
                    type = CSEventTypesConstant.INSTANT
                )
            }
            .collect {
                when (it) {
                    is WebSocket.Event.OnConnectionOpened<*> -> {
                        logger.debug { "CSNetworkManager#observeSocketConnectionState - Socket State: OnConnectionOpened" }
                        isConnected.set(true)
                        endConnectedTime = System.currentTimeMillis()
                        connectionListener.onEventChanged(OnConnectionConnected)

                        recordNonErrorHealthEvent(
                            eventName = CSEventNamesConstant.Instant.ClickStreamConnectionSuccess.value,
                            type = CSEventTypesConstant.INSTANT,
                            timeToConnection = endConnectedTime - startConnectingTime
                        )
                    }
                    is WebSocket.Event.OnMessageReceived -> {
                        logger.debug { "CSNetworkManager#observeSocketConnectionState - Socket State: OnMessageReceived : " + it.message }
                        isConnected.set(true)

                        connectionListener.onEventChanged(OnMessageReceived(it.message.mapTo()))
                    }
                    is WebSocket.Event.OnConnectionClosing -> {
                        logger.debug { "CSNetworkManager#observeSocketConnectionState - Socket State: OnConnectionClosing. Due to" + it.shutdownReason }

                        connectionListener.onEventChanged(OnConnectionClosing(it.shutdownReason.mapTo()))
                    }
                    is WebSocket.Event.OnConnectionClosed -> {
                        logger.debug { "CSNetworkManager#observeSocketConnectionState - Socket State: OnConnectionClosed. Due to" + it.shutdownReason }
                        isConnected.set(false)
                        connectionListener.onEventChanged(OnConnectionClosed(it.shutdownReason.mapTo()))

                        recordErrorHealthEvent(
                            eventName = CSEventNamesConstant.Instant.ClickStreamConnectionDropped.value,
                            throwable = Exception(it.shutdownReason.reason),
                            failureMessage = it.shutdownReason.reason
                        )
                    }
                    is WebSocket.Event.OnConnectionFailed -> {
                        logger.debug { "CSNetworkManager#observeSocketConnectionState - Socket State: OnConnectionFailed. Due to " + it.throwable }
                        isConnected.set(false)
                        endConnectedTime = System.currentTimeMillis()
                        connectionListener.onEventChanged(OnConnectionFailed(it.throwable))

                        recordErrorHealthEvent(
                            eventName = CSEventNamesConstant.Instant.ClickStreamConnectionFailure.value,
                            throwable = it.throwable,
                            failureMessage = it.throwable.message,
                            timeToConnection = endConnectedTime - startConnectingTime
                        )
                    }
                }
            }
    }

    private suspend fun recordErrorHealthEvent(
        eventName: String,
        throwable: Throwable,
        failureMessage: String?,
        timeToConnection: Long = 0L
    ) {
        val healthEvent: CSHealthEventDTO = when {
            failureMessage?.contains(CSErrorReasons.USER_UNAUTHORIZED, true) ?: false -> {
                CSHealthEventDTO(
                    eventName = eventName,
                    eventType = CSEventTypesConstant.INSTANT,
                    error = CSErrorReasons.USER_UNAUTHORIZED,
                    appVersion = info.appInfo.appVersion,
                    timeToConnection = timeToConnection
                )
            }
            throwable is SocketTimeoutException -> {
                CSHealthEventDTO(
                    eventName = eventName,
                    eventType = CSEventTypesConstant.INSTANT,
                    error = CSErrorReasons.SOCKET_TIMEOUT,
                    appVersion = info.appInfo.appVersion,
                    timeToConnection = timeToConnection
                )
            }
            throwable is EOFException -> {
                CSHealthEventDTO(
                    eventName = eventName,
                    eventType = CSEventTypesConstant.INSTANT,
                    error = CSErrorReasons.EOFException,
                    appVersion = info.appInfo.appVersion,
                    timeToConnection = timeToConnection
                )
            }
            failureMessage?.isNotEmpty() == true -> {
                CSHealthEventDTO(
                    eventName = eventName,
                    eventType = CSEventTypesConstant.INSTANT,
                    error = failureMessage,
                    appVersion = info.appInfo.appVersion,
                    timeToConnection = timeToConnection
                )
            }
            else -> {
                CSHealthEventDTO(
                    eventName = eventName,
                    eventType = CSEventTypesConstant.INSTANT,
                    error = failureMessage ?: CSErrorReasons.UNKNOWN,
                    appVersion = info.appInfo.appVersion,
                    timeToConnection = timeToConnection
                )
            }
        }
        logger.debug { "CSNetworkManager#trackConnectionFailure due to ${healthEvent.error}" }
        healthEventRepository.insertHealthEvent(healthEvent = healthEvent)
    }

    private suspend fun recordNonErrorHealthEvent(
        eventName: String,
        type: String,
        timeToConnection: Long = 0L
    ) {
        val event = CSHealthEventDTO(
            eventName = eventName,
            eventType = type,
            appVersion = info.appInfo.appVersion,
            timeToConnection = timeToConnection
        )
        healthEventRepository.insertHealthEvent(healthEvent = event)
    }
}
