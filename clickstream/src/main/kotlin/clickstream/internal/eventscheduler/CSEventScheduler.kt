package clickstream.internal.eventscheduler

import clickstream.CSEvent
import clickstream.api.CSInfo
import clickstream.config.CSEventSchedulerConfig
import clickstream.config.CSRemoteConfig
import clickstream.eventName
import clickstream.health.constant.CSErrorConstant
import clickstream.health.constant.CSEventNamesConstant
import clickstream.health.constant.CSEventTypesConstant
import clickstream.health.intermediate.CSEventHealthListener
import clickstream.health.intermediate.CSHealthEventRepository
import clickstream.health.model.CSHealthEvent
import clickstream.health.model.CSHealthEventDTO
import clickstream.internal.CSEventInternal
import clickstream.internal.lifecycle.CSAppLifeCycle
import clickstream.internal.lifecycle.CSSocketConnectionManager
import clickstream.internal.lifecycle.CSLifeCycleManager
import clickstream.internal.networklayer.CSNetworkManager
import clickstream.internal.utils.CSBatteryLevel
import clickstream.internal.utils.CSBatteryStatusObserver
import clickstream.internal.utils.CSGuIdGenerator
import clickstream.internal.utils.CSNetworkStatusObserver
import clickstream.internal.utils.CSResult
import clickstream.internal.utils.CSTimeStampGenerator
import clickstream.internal.utils.CSTimeStampMessageBuilder
import clickstream.internal.utils.flowableTicker
import clickstream.isValidMessage
import clickstream.listener.CSEventListener
import clickstream.listener.CSEventModel
import clickstream.logger.CSLogger
import clickstream.protoName
import clickstream.toFlatMap
import clickstream.report.CSReportDataTracker
import com.gojek.clickstream.de.EventRequest
import com.gojek.clickstream.internal.Health
import com.google.protobuf.MessageLite
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.cancel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import java.util.concurrent.CopyOnWriteArrayList

/**
 * The EventScheduler acts an bridge between the EventProcessor & NetworkManager
 *
 * It caches the events delivered to it. Based on the provided configuration, it processes
 * the events into batch at regular intervals
 *
 * @param eventRepository - To cache the data
 * @param networkManager - to send the analytic data to server
 * @param config - config how the scheduler should process the events
 * @param dispatcher - CoroutineDispatcher on which the events are observed
 * @param logger - To create logs
 * @param healthEventRepository - Used for logging health events
 * @param guIdGenerator - Used for generating a random ID
 * @param timeStampGenerator - Used for generating current time stamp
 * @param batteryStatusObserver - observes the battery status
 */
@ExperimentalCoroutinesApi
internal open class CSEventScheduler(
    appLifeCycleObserver: CSAppLifeCycle,
    protected val networkManager: CSNetworkManager,
    protected val dispatcher: CoroutineDispatcher,
    protected val config: CSEventSchedulerConfig,
    protected val eventRepository: CSEventRepository,
    protected val healthEventRepository: CSHealthEventRepository,
    protected val logger: CSLogger,
    private val guIdGenerator: CSGuIdGenerator,
    private val timeStampGenerator: CSTimeStampGenerator,
    private val batteryStatusObserver: CSBatteryStatusObserver,
    private val networkStatusObserver: CSNetworkStatusObserver,
    private val info: CSInfo,
    private val eventHealthListener: CSEventHealthListener,
    private val eventListeners: List<CSEventListener>,
    private val errorListener: CSEventSchedulerErrorListener,
    private val csReportDataTracker: CSReportDataTracker?,
    private val batchSizeRegulator: CSEventBatchSizeStrategy,
    private val socketConnectionManager: CSSocketConnectionManager,
    private val remoteConfig: CSRemoteConfig,
) : CSLifeCycleManager(appLifeCycleObserver) {

    protected var job: CompletableJob = SupervisorJob()
    protected var coroutineScope: CoroutineScope = CoroutineScope(job + dispatcher)
    protected val handler = CoroutineExceptionHandler { _, throwable ->
        logger.debug { throwable.message.toString() }
        errorListener.onError(tag, throwable)
    }
    private var eventData: List<CSEventData> = CopyOnWriteArrayList()
    private val appPrefix = config.eventTypePrefix
    private var isForegroundFlushCompleted = false

    override val tag: String
        get() = "CSEventScheduler"

    init {
        logger.debug { "$tag#init" }
        addObserver()
    }

    override fun onStart() {
        csReportDataTracker?.trackMessage(tag, "onStart")
        logger.debug { "$tag#onStart" }
        job = SupervisorJob()
        coroutineScope = CoroutineScope(job + dispatcher)
        socketConnectionManager.connect()
        setupObservers()
        setupTicker()
        resetOnGoingData()
    }

    override fun onStop() {
        csReportDataTracker?.trackMessage(tag, "onStop")
        logger.debug { "$tag#onStop" }
        socketConnectionManager.disconnect()
        coroutineScope.cancel()
    }

    /**
     * Converts the proto data into [CSEventData] and cache the data
     *
     * @param event [CSEvent] which holds guid, timestamp and message
     */
    open suspend fun scheduleEvent(event: CSEventInternal) {
        logger.debug { "$tag#scheduleEvent" }
        batchSizeRegulator.logEvent(event)
        val (eventName, eventProperties) = when (event) {
            is CSEventInternal.CSEvent -> {
                if (validateMessage(event.message).not()) {
                    logger.debug { "$tag#scheduleEvent - message not valid" }
                    return
                }
                getEmptySafeEventName(event.message) to event.message.toFlatMap()
            }
            is CSEventInternal.CSBytesEvent -> {
                event.eventName to mapOf()
            }
        }

        val (eventData, eventHealthData) = CSEventData.create(event)
        eventHealthListener.onEventCreated(eventHealthData)
        eventRepository.insertEventData(eventData)
        dispatchToEventListener {
            listOf(
                CSEventModel.Event.Scheduled(
                    eventId = eventData.eventGuid,
                    eventName = eventName,
                    productName = eventName,
                    properties = eventProperties,
                    timeStamp = eventData.eventTimeStamp
                )
            )
        }
        logHealthEvent(
            CSHealthEventDTO(
                eventName = CSEventNamesConstant.AggregatedAndFlushed.ClickStreamEventCached.value,
                eventType = CSEventTypesConstant.AGGREGATE,
                eventGuid = eventData.eventGuid,
                appVersion = info.appInfo.appVersion
            )
        )
    }

    /**
     * Converts the proto data into [CSEventData] and immediately forwards to the network layer
     * This acts like Fire and Forget
     *
     * @param event [CSEvent] which holds guid, timestamp and message
     */
    open fun sendInstantEvent(event: CSEventInternal) {
        logger.debug { "$tag#sendInstantEvent" }
        coroutineScope.launch(handler) {
            ensureActive()
            val (eventName, eventProperties) = when (event) {
                is CSEventInternal.CSEvent -> {
                    if (validateMessage(event.message).not()) {
                        logger.debug { "$tag#sendInstantEvent - message not valid" }
                        return@launch
                    }
                    getEmptySafeEventName(event.message) to event.message.toFlatMap()
                }
                is CSEventInternal.CSBytesEvent -> {
                    event.eventName to mapOf()
                }
            }

            val (eventData, eventHealthData) = CSEventData.create(event)
            eventHealthListener.onEventCreated(eventHealthData)
            dispatchToEventListener {
                listOf(
                    CSEventModel.Event.Instant(
                        eventId = eventData.eventGuid,
                        eventName = eventName,
                        productName = eventName,
                        properties = eventProperties,
                        timeStamp = eventData.eventTimeStamp
                    )
                )
            }
            val eventRequest =
                transformToEventRequest(eventData = listOf(eventData))
            networkManager.processInstantEvent(eventRequest)
        }
    }

    /**
     * Sets up the observers to listen the event data and response from network manager
     */
    fun setupObservers() {
        logger.debug { "$tag#setupObservers" }

        coroutineScope.launch(handler) {
            ensureActive()
            networkManager.eventGuidFlow.collect {
                when (it) {
                    is CSResult.Success -> {
                        dispatchSuccessToEventListener(it.value)
                        eventRepository.deleteEventDataByGuId(it.value)
                        csReportDataTracker?.trackSuccess(tag, it.value)
                        logger.debug {
                            "$tag#setupObservers - " +
                                    "Event Request sent successfully and deleted from DB: ${it.value}"
                        }
                    }
                    is CSResult.Failure -> {
                        eventRepository.resetOnGoingForGuid(it.value)
                        csReportDataTracker?.trackFailure(tag, it.value, it.exception)
                        logger.debug {
                            "$tag#setupObservers - " +
                                    "Event Request failed due to: ${it.exception.message}"
                        }
                    }
                }
            }
        }
    }

    private fun setupTicker() {
        logger.debug { "$tag#setupTicker" }

        coroutineScope.launch(handler) {
            ensureActive()
            flowableTicker(initialDelay = 10, delayMillis = config.batchPeriod)
                .onEach {
                    logger.debug { "$tag#setupTicker - tick" }
                }
                .collect {
                    val batch = getEventBatchToSendToServer()
                    if (batch.isEmpty()) {
                        return@collect
                    }

                    logger.debug { "$tag#setupTicker#collect - batch of ${batch.size} events: $batch" }

                    forwardEvents(batch)
                }
        }
    }

    /**
     * Processes the batch and converts into EventRequest and then
     * forwards it to the NetworkManager
     */
    protected open suspend fun forwardEvents(
        batch: List<CSEventData>,
        forFlushing: Boolean = false
    ): String? {

        logger.debug { "$tag#forwardEvents" }

        if (isInvalidBatteryLevel(forFlushing)) {
            return isBatteryLow()
        }

        if (networkStatusObserver.isNetworkAvailable().not()) {
            return isNetworkAvailable()
        }

        if (!networkManager.isSocketAvailable()) {
            return isSocketConnected()
        }

        dispatchToEventListener {
            batch.map { csEvent ->
                CSEventModel.Event.Dispatched(
                    eventId = csEvent.eventGuid,
                    eventName = getEmptySafeEventName(csEvent.event(appPrefix)),
                    productName = csEvent.event(appPrefix).protoName(),
                    timeStamp = csEvent.eventTimeStamp
                )
            }
        }

        val eventRequest =
            transformToEventRequest(eventData = batch)
        if (batch.isNotEmpty() && batch[0].messageName != Health::class.qualifiedName.orEmpty()) {
            logger.debug {
                "$tag#forwardEvents#batch - " +
                        "eventBatchId : ${eventRequest.reqGuid}, " +
                        "eventId : ${batch.joinToString { it.eventGuid }}"
            }
            logger.debug {
                "$tag#forwardEvents#batch - " +
                        "messageName : ${batch[0].messageName}"
            }
            logHealthEvent(
                CSHealthEventDTO(
                    eventName = CSEventNamesConstant.AggregatedAndFlushed.ClickStreamEventBatchCreated.value,
                    eventType = CSEventTypesConstant.AGGREGATE,
                    eventBatchGuid = eventRequest.reqGuid,
                    eventGuid = batch.joinToString { it.eventGuid },
                    appVersion = info.appInfo.appVersion
                )
            )

            csReportDataTracker?.trackDupData(tag, batch)
        }

        networkManager.processEvent(eventRequest = eventRequest)
        updateEventsGuidAndInsert(eventRequest, batch)
        return eventRequest.reqGuid
    }

    private suspend fun isInvalidBatteryLevel(forFlushing: Boolean): Boolean {
        return if (forFlushing && remoteConfig.ignoreBatteryLvlOnFlush) {
            false
        } else {
            batteryStatusObserver.getBatteryStatus() == CSBatteryLevel.LOW_BATTERY
        }
    }

    private fun transformToEventRequest(eventData: List<CSEventData>): EventRequest {
        logger.debug { "$tag#transformToEventRequest" }

        return EventRequest.newBuilder().apply {
            reqGuid = guIdGenerator.getId()
            sentTime = CSTimeStampMessageBuilder.build(timeStampGenerator.getTimeStamp())
            addAllEvents(eventData.map { it.event(appPrefix) })
        }.build()
    }

    private suspend fun updateEventsGuidAndInsert(
        eventRequest: EventRequest,
        eventData: List<CSEventData>
    ) {
        logger.debug { "CSEventSchedulerDeDup#updateEventsGuidAndInsert" }

        val updatedList =
            eventData.map { it.copy(eventRequestGuid = eventRequest.reqGuid, isOnGoing = true) }
                .toList()
        eventRepository.updateEventDataList(updatedList)
    }

    private suspend fun logHealthEvent(event: CSHealthEventDTO) {
        logger.debug { "$tag#logHealthEvent" }

        healthEventRepository.insertHealthEvent(event)
    }

    private fun resetOnGoingData() {
        logger.debug { "$tag#resetOnGoingData" }

        coroutineScope.launch(handler) {
            ensureActive()
            val onGoingEvents = eventRepository.getOnGoingEvents()
            val data = onGoingEvents.map { it.copy(isOnGoing = false) }.toList()
            eventRepository.insertEventDataList(data)
        }
    }

    private suspend fun validateMessage(message: MessageLite): Boolean {
        logger.debug { "$tag#validateMessage" }

        val isValid = config.utf8ValidatorEnabled && message.isValidMessage()
        if (isValid.not()) {
            logHealthEvent(
                CSHealthEventDTO(
                    eventName = "Invalid message",
                    eventType = CSEventTypesConstant.AGGREGATE,
                    error = message.protoName(),
                    appVersion = info.appInfo.appVersion
                )
            )
        }
        return isValid
    }

    private suspend fun isSocketConnected(): String? {
        logHealthEvent(
            CSHealthEventDTO(
                eventName = CSEventNamesConstant.Instant.ClickStreamEventBatchTriggerFailed.value,
                eventType = CSEventTypesConstant.AGGREGATE,
                error = CSErrorConstant.SOCKET_NOT_OPEN,
                appVersion = info.appInfo.appVersion
            )
        )
        return null
    }

    private suspend fun isNetworkAvailable(): String? {
        logHealthEvent(
            CSHealthEventDTO(
                eventName = CSEventNamesConstant.Instant.ClickStreamEventBatchTriggerFailed.value,
                eventType = CSEventTypesConstant.AGGREGATE,
                error = CSErrorConstant.NETWORK_UNAVAILABLE,
                appVersion = info.appInfo.appVersion
            )
        )
        return null
    }

    private suspend fun isBatteryLow(): String? {
        logHealthEvent(
            CSHealthEventDTO(
                eventName = CSEventNamesConstant.Instant.ClickStreamEventBatchTriggerFailed.value,
                eventType = CSEventTypesConstant.AGGREGATE,
                error = CSErrorConstant.LOW_BATTERY,
                appVersion = info.appInfo.appVersion
            )
        )
        return null
    }

    private fun dispatchToEventListener(evaluator: () -> List<CSEventModel>) {
        if (eventListeners.isEmpty()) {
            return
        }
        coroutineScope.launch {
            eventListeners.forEach {
                it.onCall(evaluator())
            }
        }
    }

    /**
     * Fetched and dispatches events loaded by request id to event listener.
     * Since this is called just before we call [eventRepository.deleteEventDataByGuId]
     * in [setupObservers], this is a blocking call.
     *
     * @param requestId
     */
    protected suspend fun dispatchSuccessToEventListener(requestId: String) {
        if (eventListeners.isEmpty()) {
            return
        }
        val currentRequestIdEvents = eventRepository.loadEventsByRequestId(requestId)
        dispatchToEventListener {
            currentRequestIdEvents.map { csEvent ->
                CSEventModel.Event.Acknowledged(
                    eventId = csEvent.eventGuid,
                    eventName = getEmptySafeEventName(csEvent.event(appPrefix)),
                    productName = csEvent.event(appPrefix).protoName(),
                    timeStamp = csEvent.eventTimeStamp
                )
            }
        }
    }

    /**
     * If eventName is null or empty, this will return the protoName instead.
     *
     * */
    private fun getEmptySafeEventName(message: MessageLite): String {
        val eventName = message.eventName()
        return if (eventName.isNullOrEmpty()) {
            message.protoName()
        } else {
            eventName
        }
    }

    private suspend fun getEventBatchToSendToServer(): List<CSEventData> {
        return if (shouldFlushInForeground()) {
            val batch = eventRepository.getAllUnprocessedEvents()
            logger.debug { "$tag#foregroundFlushing: ${batch.size} events" }
            isForegroundFlushCompleted = true
            logHealthEvent(
                CSHealthEventDTO(
                    eventName = CSEventNamesConstant.AggregatedAndFlushed.ClickStreamFlushOnForeground.value,
                    eventType = CSEventTypesConstant.AGGREGATE,
                    eventGuid = batch.joinToString { it.eventGuid },
                    appVersion = info.appInfo.appVersion,
                    count = batch.size
                )
            )
            batch
        } else {
            getEventsForBatch()
        }
    }

    // Do not Flush in foreground if flushing with batching config flag is enabled.
    // Flushing with batching is as good as normal batching.
    private fun shouldFlushInForeground() =
        config.enableForegroundFlushing && !isForegroundFlushCompleted &&
                networkManager.isSocketAvailable() && !remoteConfig.batchFlushedEvents

    private suspend fun getEventsForBatch(): List<CSEventData> {
        val countOfEventsPerBatch =
            batchSizeRegulator.regulatedCountOfEventsPerBatch(config.eventsPerBatch)
        return eventRepository.getUnprocessedEventsWithLimit(countOfEventsPerBatch)
    }
}