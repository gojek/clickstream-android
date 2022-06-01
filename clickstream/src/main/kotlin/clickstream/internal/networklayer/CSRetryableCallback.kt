package clickstream.internal.networklayer

import clickstream.api.CSInfo
import clickstream.config.CSNetworkConfig
import clickstream.extension.isHealthEvent
import clickstream.health.constant.CSEventNamesConstant
import clickstream.health.constant.CSEventTypesConstant
import clickstream.health.intermediate.CSHealthEventRepository
import clickstream.health.model.CSHealthEventDTO
import clickstream.health.time.CSTimeStampGenerator
import clickstream.internal.analytics.CSErrorReasons
import clickstream.internal.utils.CSTimeStampMessageBuilder
import clickstream.logger.CSLogger
import com.gojek.clickstream.de.EventRequest
import com.gojek.clickstream.de.common.Code
import com.gojek.clickstream.de.common.EventResponse
import com.gojek.clickstream.de.common.Status
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

private const val REQUEST_GUID_KEY = "req_guid"

/**
 * The request to the server is made via the [CSRetryableCallback].
 * The RequestCallback handles retries whenever the ack received is
 * failure or when a timeout occurs.
 *
 * The final result is communicated using the [onSuccess] and [onFailure] methods.
 */
internal abstract class CSRetryableCallback(
    private val networkConfig: CSNetworkConfig,
    private val eventService: CSEventService,
    private var eventRequest: EventRequest,
    private val dispatcher: CoroutineDispatcher,
    private val timeStampGenerator: CSTimeStampGenerator,
    private val logger: CSLogger,
    private val healthEventRepository: CSHealthEventRepository,
    private val info: CSInfo,
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + dispatcher)
) {

    /**
     * Triggered when the acknowledgment received holds the state as SUCCESS
     *
     * @param guid The batch guid for which the request was triggered.
     */
    abstract fun onSuccess(guid: String)

    /**
     * Triggered when the acknowledgment received holds the state as FAILURE/ERROR
     *
     * @param throwable The exception which holds the reason for the failure.
     */
    abstract fun onFailure(throwable: Throwable, guid: String)

    private var timeOutJob: Job = SupervisorJob()
    private var retryCount: AtomicInteger = AtomicInteger(0)

    init {
        logger.debug {
            "CSRetryableCallback#init"
        }

        coroutineScope.launch {
            launch { observeCallback() }
            launch { sendEvent() }
        }
    }

    /**
     * Listens to socket connection. When the data received corresponds to
     * the current request, it checks whether the request has failed.
     *
     * If fails, it retries the request.
     * If success, it invokes onSuccess callback
     */
    private suspend fun observeCallback() {
        logger.debug {
            "CSRetryableCallback#observeCallback"
        }

        eventService.observeResponse().onEach { response ->
            logger.debug {
                "CSRetryableCallback#observeCallback#onEach - response $response"
            }
        }.filter {
            it.dataMap[REQUEST_GUID_KEY] == eventRequest.reqGuid
        }.collect {
            logger.debug {
                "CSRetryableCallback#observeCallback - Message received from the server: ${it.dataMap[REQUEST_GUID_KEY]}"
            }
            val guid = it.dataMap[REQUEST_GUID_KEY]!!
            when {
                it.status == Status.SUCCESS -> {
                    logger.debug {
                        "CSRetryableCallback#observeCallback - Success"
                    }

                    onSuccess(guid)
                    sendAckAndComplete()
                }
                shouldRetry() -> {
                    logger.debug {
                        "CSRetryableCallback#observeCallback - retried"
                    }

                    trackEventResponse(it, eventRequest.reqGuid)
                    retry()
                }
                else -> {
                    logger.debug {
                        "CSRetryableCallback#observeCallback - else"
                    }

                    trackEventResponse(it, eventRequest.reqGuid)
                    onFailure(Throwable(), guid)
                    onComplete()
                }
            }
        }
    }

    /**
     * The request is sent to the server with the given event request.
     * Once the request is sent, the data is logged and also sent to CT
     */
    private suspend fun sendEvent() {
        if (eventService.sendEvent(eventRequest)) {
            logBatchSentEvent()
            logger.debug {
                "CSRetryableCallback#sendEvent - Request successfully sent to the server: $eventRequest"
            }
        } else {
            logger.debug {
                "CSRetryableCallback#sendEvent - Request sent to the server failed: ${eventRequest.reqGuid}"
            }
            recordHealthEvent(
                eventName = CSEventNamesConstant.ClickStreamBatchWriteFailed.value,
                eventType = CSEventTypesConstant.AGGREGATE,
                eventBatchId = eventRequest.reqGuid,
                error = "Batch write failed"
            )
        }

        observeTimeout()
    }

    /**
     * Waits for x duration [CSNetworkConfig.maxRequestAckTimeout] and then invokes retry if the duration
     * exceeds. This is marked as timeout.
     *
     * Teh existing timeout job [timeOutJob] is cancelled and new one is created.
     */
    private suspend fun observeTimeout() {
        logger.debug {
            "CSRetryableCallback#observeTimeout"
        }

        if (timeOutJob.isActive) {
            timeOutJob.cancel()
        }
        timeOutJob = coroutineScope.launch(dispatcher) {
            delay(networkConfig.maxRequestAckTimeout)
            logger.debug {
                "CSRetryableCallback#observeTimeout - Acknowledgment Timeout.  Triggering the request again"
            }

            recordHealthEvent(
                eventName = CSEventNamesConstant.ClickStreamEventBatchTimeout.value,
                eventType = CSEventTypesConstant.AGGREGATE,
                eventBatchId = eventRequest.reqGuid,
                error = "SocketTimeout"
            )
            if (shouldRetry()) {
                retry()
            } else {
                onFailure(Throwable(), eventRequest.reqGuid)
            }
        }
    }

    /**
     * The request is retried. The retry count is incremented by one and the request is sent again.
     */
    private suspend fun retry() {
        logger.debug {
            "CSRetryableCallback#retry"
        }

        retryCount.incrementAndGet()
        eventRequest = eventRequest.toBuilder().apply {
            sentTime = CSTimeStampMessageBuilder.build(timeStampGenerator.getTimeStamp())
        }.build()

        sendEvent()
    }

    /**
     * Decides whether the retry should be performed or not
     */
    private fun shouldRetry(): Boolean {
        val shouldRetry = retryCount.get() < networkConfig.maxRetriesPerBatch

        logger.debug {
            "CSRetryableCallback#shouldRetry - shouldRetry $shouldRetry"
        }
        return shouldRetry
    }

    /**
     * Logs the batch sent to the CT
     */
    private fun logBatchSentEvent() {
        logger.debug {
            "CSRetryableCallback#logBatchSentEvent"
        }

        coroutineScope.launch(dispatcher) {
            recordHealthEvent(
                eventName = CSEventNamesConstant.ClickStreamBatchSent.value,
                eventType = CSEventTypesConstant.AGGREGATE,
                eventBatchId = eventRequest.reqGuid,
                error = ""
            )
        }
    }

    /**
     * Send the Ack event and then invokes complete
     */
    private suspend fun sendAckAndComplete() {
        logger.debug {
            "CSRetryableCallback#sendAckAndComplete"
        }

        recordHealthEvent(
            eventName = CSEventNamesConstant.ClickStreamEventBatchAck.value,
            eventType = CSEventTypesConstant.AGGREGATE,
            eventBatchId = eventRequest.reqGuid,
            error = ""
        )
        onComplete()
    }

    /**
     * Cancels the existing job and coroutine scope when the request is processed.
     */
    private fun onComplete() {
        logger.debug {
            "CSRetryableCallback#onComplete"
        }

        coroutineScope.cancel()
        timeOutJob.cancel()
    }

    private suspend fun trackEventResponse(eventResponse: EventResponse, eventRequestGuid: String) {
        logger.debug {
            "CSRetryableCallback#trackEventResponse - eventResponse $eventResponse"
        }

        when (eventResponse.code.ordinal) {
            Code.MAX_CONNECTION_LIMIT_REACHED.ordinal -> {
                logger.debug {
                    "CSRetryableCallback#trackEventResponse - eventResponse MAX_CONNECTION_LIMIT_REACHED"
                }

                recordHealthEvent(
                    eventName = CSEventNamesConstant.ClickStreamConnectionFailed.value,
                    eventType = CSEventTypesConstant.AGGREGATE,
                    error = CSErrorReasons.MAX_CONNECTION_LIMIT_REACHED,
                    eventBatchId = eventRequestGuid
                )
            }
            Code.MAX_USER_LIMIT_REACHED.ordinal -> {
                logger.debug {
                    "CSRetryableCallback#trackEventResponse - eventResponse MAX_USER_LIMIT_REACHED"
                }

                recordHealthEvent(
                    eventName = CSEventNamesConstant.ClickStreamConnectionFailed.value,
                    eventType = CSEventTypesConstant.AGGREGATE,
                    error = CSErrorReasons.MAX_USER_LIMIT_REACHED,
                    eventBatchId = eventRequestGuid
                )
            }
            Code.BAD_REQUEST.ordinal -> {
                logger.debug {
                    "CSRetryableCallback#trackEventResponse - eventResponse BAD_REQUEST"
                }

                recordHealthEvent(
                    eventName = CSEventNamesConstant.ClickStreamEventBatchErrorResponse.value,
                    eventType = CSEventTypesConstant.AGGREGATE,
                    error = CSErrorReasons.PARSING_EXCEPTION,
                    eventBatchId = eventRequestGuid
                )
            }
            else -> {
                logger.debug {
                    "CSRetryableCallback#trackEventResponse - eventResponse ClickStreamEventBatchErrorResponse"
                }

                recordHealthEvent(
                    eventName = CSEventNamesConstant.ClickStreamEventBatchErrorResponse.value,
                    eventType = CSEventTypesConstant.AGGREGATE,
                    error = CSErrorReasons.UNKNOWN,
                    eventBatchId = eventRequestGuid
                )
            }
        }
    }

    private suspend fun recordHealthEvent(
        eventName: String,
        eventType: String,
        error: String,
        eventBatchId: String,
    ) {
        logger.debug {
            StringBuilder()
                .append("CSRetryableCallback#recordHealthEvent - events : ${eventRequest.eventsList}")
                .apply {
                    if (eventRequest.eventsCount > 0) {
                        append("isHealthEvent : ${eventRequest.getEvents(0).isHealthEvent()}")
                    }
                }.toString()
        }

        if (eventRequest.eventsCount > 0 && eventRequest.getEvents(0).isHealthEvent().not()) {
            logger.debug {
                "CSRetryableCallback#recordHealthEvent - insertHealthEvent"
            }

            healthEventRepository.insertHealthEvent(
                CSHealthEventDTO(
                    eventName = eventName,
                    eventType = eventType,
                    eventBatchId = eventBatchId,
                    error = error,
                    appVersion = info.appInfo.appVersion
                )
            )
        }
    }
}