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
import clickstream.health.constant.CSErrorConstant
import clickstream.health.constant.CSEventNamesConstant
import clickstream.health.constant.CSEventTypesConstant
import clickstream.health.intermediate.CSHealthEventRepository
import clickstream.health.model.CSHealthEventDTO
import clickstream.internal.lifecycle.CSAppLifeCycle
import clickstream.internal.lifecycle.CSLifeCycleManager
import clickstream.internal.utils.CSResult
import clickstream.listener.CSEventListener
import clickstream.listener.CSEventModel
import clickstream.logger.CSLogger
import clickstream.report.CSReportDataTracker
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
    private val connectionListener: CSSocketConnectionListener,
    private val csReportDataTracker: CSReportDataTracker?,
    private val csEventListeners: List<CSEventListener>,
) : CSLifeCycleManager(appLifeCycleObserver) {

    private val isConnected: AtomicBoolean = AtomicBoolean(false)
    protected var job: CompletableJob = SupervisorJob()
    protected var coroutineScope: CoroutineScope = CoroutineScope(job + dispatcher)
    private val handler = CoroutineExceptionHandler { _, throwable ->
        logger.debug { "$tag#handler : ${throwable.message}" }
    }

    @VisibleForTesting
    var connectionStartTime = 0L

    @VisibleForTesting
    var connectionEndTime = 0L

    override val tag: String
        get() = "CSNetworkManager"

    init {
        logger.debug { "$tag#init" }
        addObserver()
    }

    /**
     * Provides status of network manager
     *
     * @return isAvailable - Whether network manager is available or not
     */
    fun isSocketAvailable(): Boolean {
        logger.debug { "$tag#isAvailable - isSocketConnected : ${isConnected.get()}" }

        return isConnected.get()
    }

    /**
     * Updates the caller with the status of the request
     */
    private lateinit var callback: CSEventGuidCallback

    val eventGuidFlow: Flow<CSResult<String>> = callbackFlow {
        logger.debug { "$tag#eventGuidFlow" }

        callback = object : CSEventGuidCallback {
            override fun onSuccess(data: String) {
                logger.debug { "$tag#eventGuidFlow#onSuccess - $data" }

                offer(CSResult.Success(data))
            }

            override fun onError(error: Throwable, guid: String) {
                error.printStackTrace()
                logger.debug { "$tag#eventGuidFlow#onError - $guid errorMessage : ${error.message}" }

                offer(CSResult.Failure(error, guid))
            }
        }
        awaitClose()
    }

    override fun onStart() {
        logger.debug { "$tag#onStart" }
        job = SupervisorJob()
        coroutineScope = CoroutineScope(job + dispatcher)
        observeSocketState()
    }

    override fun onStop() {
        logger.debug { "$tag#onStop" }
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
        logger.debug { "$tag#processEvent" }
        networkRepository.sendEvents(eventRequest = eventRequest, callback = callback)
    }

    /**
     * The instant/health data which is sent to the server without
     *
     * @param eventRequest - The data which hold the analytic events
     */
    fun processInstantEvent(
        eventRequest: EventRequest
    ) {
        logger.debug { "$tag#processInstantEvent" }
        networkRepository.sendInstantEvents(eventRequest = eventRequest)
    }

    /**
     * Observes the web socket connection state.
     *
     * When the connection is closed, the scope is cancelled to unsubscribe the events
     */
    protected fun observeSocketState(): Job {
        logger.debug { "$tag#observeSocketState" }
        return coroutineScope.launch(handler) {
            logger.debug { "$tag#observeSocketState is coroutine active : $isActive" }
            ensureActive()
            networkRepository.observeSocketState()
                .onStart {
                    connectionListener.onEventChanged(OnConnectionConnecting)
                    trackConnectionAttemptHealth()
                    connectionStartTime = System.currentTimeMillis()
                }
                .collect {
                    when (it) {
                        is WebSocket.Event.OnConnectionOpened<*> -> {
                            logger.debug { "$tag#observeSocketState - Socket State: OnConnectionOpened" }
                            connectionEndTime = System.currentTimeMillis()
                            isConnected.set(true)
                            csReportDataTracker?.trackMessage(tag, "Socket Connected")
                            connectionListener.onEventChanged(OnConnectionConnected)
                            trackConnectionSuccessHealth()
                        }
                        is WebSocket.Event.OnMessageReceived -> {
                            logger.debug { "$tag#observeSocketState - Socket State: OnMessageReceived : " + it.message }
                            isConnected.set(true)
                            dispatchSocketConnectionStatusToEv(true)
                            connectionListener.onEventChanged(OnMessageReceived(it.message.mapTo()))
                        }
                        is WebSocket.Event.OnConnectionClosing -> {
                            connectionListener.onEventChanged(OnConnectionClosing(it.shutdownReason.mapTo()))
                            csReportDataTracker?.trackMessage(
                                tag,
                                "\"ConnectionClosing due to ${it.shutdownReason}\""
                            )
                            logger.debug { "$tag#observeSocketState - Socket State: OnConnectionClosing. Due to" + it.shutdownReason }
                        }
                        is WebSocket.Event.OnConnectionClosed -> {
                            logger.debug { "$tag#observeSocketState - Socket State: OnConnectionClosed. Due to" + it.shutdownReason }
                            isConnected.set(false)
                            csReportDataTracker?.trackMessage(
                                tag,
                                "\"ConnectionClosed due to ${it.shutdownReason}\""
                            )
                            connectionListener.onEventChanged(OnConnectionClosed(it.shutdownReason.mapTo()))
                            dispatchSocketConnectionStatusToEv(false)
                        }
                        is WebSocket.Event.OnConnectionFailed -> {
                            logger.debug { "$tag#observeSocketState - Socket State: OnConnectionFailed. Due to " + it.throwable }
                            isConnected.set(false)
                            csReportDataTracker?.trackMessage(
                                tag,
                                "\"ConnectionFailed due to ${it.throwable.message}\""
                            )
                            connectionListener.onEventChanged(OnConnectionFailed(it.throwable))
                            dispatchSocketConnectionStatusToEv(false)
                            trackConnectionFailure(it)
                        }
                    }
                }
        }
    }

    private suspend fun trackConnectionAttemptHealth() {
        logger.debug { "$tag#trackConnectionAttempt" }
        val connAttemptDto = CSHealthEventDTO(
            eventName = CSEventNamesConstant.Instant.ClickStreamConnectionAttempt.value,
            eventType = CSEventTypesConstant.INSTANT,
            appVersion = info.appInfo.appVersion
        )
        healthEventRepository.insertHealthEvent(connAttemptDto)
    }

    private suspend fun trackConnectionSuccessHealth() {
        logger.debug { "$tag#trackConnectionSucces" }
        val connSuccessDto = CSHealthEventDTO(
            eventName = CSEventNamesConstant.Instant.ClickStreamConnectionSuccess.value,
            eventType = CSEventTypesConstant.INSTANT,
            appVersion = info.appInfo.appVersion,
            timeToConnection = connectionEndTime - connectionStartTime
        )
        healthEventRepository.insertHealthEvent(connSuccessDto)
    }

    private suspend fun trackConnectionFailure(failureResponse: WebSocket.Event.OnConnectionFailed) {
        logger.debug { "$tag#trackConnectionFailure $failureResponse" }

        val healthEvent = when {
            failureResponse.throwable.message?.contains(CSErrorConstant.USER_UNAUTHORIZED, true)
                ?: false -> {
                CSHealthEventDTO(
                    eventName = CSEventNamesConstant.Instant.ClickStreamConnectionFailure.value,
                    eventType = CSEventTypesConstant.AGGREGATE,
                    error = CSErrorConstant.USER_UNAUTHORIZED,
                    appVersion = info.appInfo.appVersion,
                )
            }
            failureResponse.throwable is SocketTimeoutException -> {
                CSHealthEventDTO(
                    eventName = CSEventNamesConstant.Instant.ClickStreamConnectionFailure.value,
                    eventType = CSEventTypesConstant.AGGREGATE,
                    error = CSErrorConstant.SOCKET_TIMEOUT,
                    appVersion = info.appInfo.appVersion
                )
            }
            failureResponse.throwable.message?.isNotEmpty() ?: false -> {
                CSHealthEventDTO(
                    eventName = CSEventNamesConstant.Instant.ClickStreamConnectionFailure.value,
                    eventType = CSEventTypesConstant.AGGREGATE,
                    error = failureResponse.throwable.toString(),
                    appVersion = info.appInfo.appVersion
                )
            }
            else -> {
                CSHealthEventDTO(
                    eventName = CSEventNamesConstant.Instant.ClickStreamConnectionFailure.value,
                    eventType = CSEventTypesConstant.AGGREGATE,
                    error = CSErrorConstant.UNKNOWN,
                    appVersion = info.appInfo.appVersion
                )
            }
        }
        healthEventRepository.insertHealthEvent(healthEvent = healthEvent)
    }

    private fun dispatchSocketConnectionStatusToEv(isConnected: Boolean) {
        coroutineScope.launch {
            csEventListeners.forEach {
                it.onCall(listOf(CSEventModel.Connection(isConnected)))
            }
        }
    }
}
