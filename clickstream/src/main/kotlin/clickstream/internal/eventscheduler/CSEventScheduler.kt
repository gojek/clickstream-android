package clickstream.internal.eventscheduler

import clickstream.CSEvent
import clickstream.config.CSEventSchedulerConfig
import clickstream.health.CSEventHealthListener
import clickstream.health.CSEventNames.ClickStreamEventBatchCreated
import clickstream.health.CSEventNames.ClickStreamEventBatchTriggerFailed
import clickstream.health.CSEventNames.ClickStreamEventCached
import clickstream.health.CSEventNames.ClickStreamInvalidMessage
import clickstream.health.CSGuIdGenerator
import clickstream.health.CSInfo
import clickstream.health.CSTimeStampGenerator
import clickstream.internal.analytics.CSErrorReasons
import clickstream.internal.networklayer.CSNetworkManager
import clickstream.internal.utils.CSBatteryLevel
import clickstream.internal.utils.CSBatteryStatusObserver
import clickstream.internal.utils.CSNetworkStatusObserver
import clickstream.internal.utils.CSResult
import clickstream.internal.utils.CSTimeStampMessageBuilder
import clickstream.internal.utils.flowableTicker
import clickstream.isValidMessage
import clickstream.lifecycle.CSAppLifeCycle
import clickstream.lifecycle.CSLifeCycleManager
import clickstream.logger.CSLogger
import com.gojek.clickstream.de.EventRequest
import com.gojek.clickstream.internal.Health
import com.google.protobuf.MessageLite
import java.util.concurrent.CopyOnWriteArrayList
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

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
    appLifeCycle: CSAppLifeCycle,
    protected val networkManager: CSNetworkManager,
    protected val dispatcher: CoroutineDispatcher,
    protected val config: CSEventSchedulerConfig,
    protected val eventRepository: CSEventRepository,
    protected val healthEventRepository: clickstream.health.CSHealthEventRepository,
    protected val logger: CSLogger,
    private val guIdGenerator: CSGuIdGenerator,
    private val timeStampGenerator: CSTimeStampGenerator,
    private val batteryStatusObserver: CSBatteryStatusObserver,
    private val networkStatusObserver: CSNetworkStatusObserver,
    private val info: CSInfo,
    private val eventHealthListener: CSEventHealthListener
) : CSLifeCycleManager(appLifeCycle) {

    protected var job: CompletableJob = SupervisorJob()
    protected var coroutineScope: CoroutineScope = CoroutineScope(job + dispatcher)
    private val handler = CoroutineExceptionHandler { _, throwable ->
        logger.debug { throwable.message.toString() }
    }
    private var eventData: List<CSEventData> = CopyOnWriteArrayList()

    init {
        logger.debug { "CSEventScheduler#init" }
        addObserver()
    }

    override fun onStart() {
        logger.debug { "CSEventScheduler#onStart" }
        job = SupervisorJob()
        coroutineScope = CoroutineScope(job + dispatcher)
        setupObservers()
        setupTicker()
        resetOnGoingData()
    }

    override fun onStop() {
        logger.debug { "CSEventScheduler#onStop" }
        coroutineScope.cancel()
    }

    /**
     * Converts the proto data into [CSEventData] and cache the data
     *
     * @param event [CSEvent] which holds guid, timestamp and message
     */
    public suspend fun scheduleEvent(event: CSEvent) {
        logger.debug { "CSEventScheduler#scheduleEvent" }
        if (validateMessage(event.message).not()) {
            logger.debug { "CSEventScheduler#scheduleEvent - message not valid" }
            return
        }

        val (eventData, eventHealthData) = CSEventData.create(event)
        eventHealthListener.onEventCreated(eventHealthData)
        eventRepository.insertEventData(eventData)

        logHealthEvent(
            clickstream.health.CSHealthEvent(
                eventName = ClickStreamEventCached.value,
                eventType = clickstream.health.EventTypes.AGGREGATE,
                eventId = eventData.eventGuid,
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
    public fun sendInstantEvent(event: CSEvent) {
        logger.debug { "CSEventScheduler#sendInstantEvent" }

        coroutineScope.launch(handler) {
            ensureActive()
            if (validateMessage(event.message).not()) {
                logger.debug { "CSEventScheduler#sendInstantEvent - message not valid" }
                return@launch
            }

            val (eventData, eventHealthData) = CSEventData.create(event)
            eventHealthListener.onEventCreated(eventHealthData)
            val eventRequest =
                transformToEventRequest(eventData = listOf(eventData))
            networkManager.processInstantEvent(eventRequest)
        }
    }

    /**
     * Sets up the observers to listen the event data and response from network manager
     */
    protected fun setupObservers() {
        logger.debug { "CSEventScheduler#setupObservers" }

        coroutineScope.launch(handler) {
            ensureActive()
            eventRepository.getEventDataList().collect {
                eventData = it
            }
        }

        coroutineScope.launch(handler) {
            ensureActive()
            networkManager.eventGuidFlow.collect {
                when (it) {
                    is CSResult.Success -> {
                        eventRepository.deleteEventDataByGuId(it.value)
                        logger.debug {
                            "CSEventScheduler#setupObservers - " +
                                    "Event Request sent successfully and deleted from DB: ${it.value}"
                        }
                    }
                    is CSResult.Failure -> {
                        eventRepository.resetOnGoingForGuid(it.value)
                        logger.debug {
                            "CSEventScheduler#setupObservers - " +
                                    "Event Request failed due to: ${it.exception.message}"
                        }
                    }
                }
            }
        }
    }

    private fun setupTicker() {
        logger.debug { "CSEventScheduler#setupTicker" }

        coroutineScope.launch(handler) {
            ensureActive()
            flowableTicker(initialDelay = 10, delayMillis = config.batchPeriod)
                .onEach {
                    logger.debug { "CSEventScheduler#setupTicker - tick" }
                }
                .collect {
                    if (eventData.isEmpty()) {
                        return@collect
                    }

                    val batch = when {
                        eventData.isEmpty() -> emptyList()
                        isEventLessThanBatchCount() -> eventData
                        else -> eventData.subList(0, config.eventsPerBatch)
                    }
                    logger.debug { "CSEventScheduler#setupTicker#collect - batch : $batch" }

                    forwardEvents(batch)
                }
        }
    }

    /**
     * Processes the batch and converts into EventRequest and then
     * forwards it to the NetworkManager
     */
    protected suspend fun forwardEvents(batch: List<CSEventData>): String? {
        logger.debug { "CSEventScheduler#forwardEvents" }

        if (batteryStatusObserver.getBatteryStatus() == CSBatteryLevel.LOW_BATTERY) {
            return isBatteryLow()
        }

        if (networkStatusObserver.isNetworkAvailable().not()) {
            return isNetworkAvailable()
        }

        if (!networkManager.isAvailable()) {
            return isSocketConnected()
        }

        val eventRequest =
            transformToEventRequest(eventData = batch)
        if (batch.isNotEmpty() && batch[0].messageName != Health::class.qualifiedName.orEmpty()) {
            logger.debug {
                "CSEventScheduler#forwardEvents#batch - " +
                        "eventBatchId : ${eventRequest.reqGuid}, " +
                        "eventId : ${batch.joinToString { it.eventGuid }}"
            }
            logger.debug {
                "CSEventScheduler#forwardEvents#batch - " +
                        "messageName : ${batch[0].messageName}"
            }
            logHealthEvent(
                clickstream.health.CSHealthEvent(
                    eventName = ClickStreamEventBatchCreated.value,
                    eventType = clickstream.health.EventTypes.AGGREGATE,
                    eventBatchId = eventRequest.reqGuid,
                    eventId = batch.joinToString { it.eventGuid },
                    appVersion = info.appInfo.appVersion
                )
            )
        }

        networkManager.processEvent(eventRequest = eventRequest)
        updateEventsGuidAndInsert(eventRequest, batch)
        return eventRequest.reqGuid
    }

    private fun isEventLessThanBatchCount() =
        config.eventsPerBatch > eventData.size

    private fun transformToEventRequest(eventData: List<CSEventData>): EventRequest {
        logger.debug { "CSEventScheduler#transformToEventRequest" }

        return EventRequest.newBuilder().apply {
            reqGuid = guIdGenerator.getId()
            sentTime = CSTimeStampMessageBuilder.build(timeStampGenerator.getTimeStamp())
            addAllEvents(eventData.map { it.event() })
        }.build()
    }

    private suspend fun updateEventsGuidAndInsert(
        eventRequest: EventRequest,
        eventData: List<CSEventData>
    ) {
        logger.debug { "CSEventScheduler#updateEventsGuidAndInsert" }

        val updatedList =
            eventData.map { it.copy(eventRequestGuid = eventRequest.reqGuid, isOnGoing = true) }
                .toList()
        eventRepository.insertEventDataList(updatedList)
    }

    private suspend fun logHealthEvent(event: clickstream.health.CSHealthEvent) {
        logger.debug { "CSEventScheduler#logHealthEvent" }

        healthEventRepository.insertHealthEvent(event)
    }

    private fun resetOnGoingData() {
        logger.debug { "CSEventScheduler#resetOnGoingData" }

        coroutineScope.launch(handler) {
            ensureActive()
            val onGoingEvents = eventRepository.getOnGoingEvents()
            val data = onGoingEvents.map { it.copy(isOnGoing = false) }.toList()
            eventRepository.insertEventDataList(data)
        }
    }

    private suspend fun validateMessage(message: MessageLite): Boolean {
        logger.debug { "CSEventScheduler#validateMessage" }

        val isValid = config.utf8ValidatorEnabled && message.isValidMessage()
        if (isValid.not()) {
            logHealthEvent(
                clickstream.health.CSHealthEvent(
                    eventName = ClickStreamInvalidMessage.value,
                    eventType = clickstream.health.EventTypes.AGGREGATE,
                    error = message.toByteString().toString(),
                    appVersion = info.appInfo.appVersion
                )
            )
        }
        return isValid
    }

    private suspend fun isSocketConnected(): String? {
        logHealthEvent(
            clickstream.health.CSHealthEvent(
                eventName = ClickStreamEventBatchTriggerFailed.value,
                eventType = clickstream.health.EventTypes.AGGREGATE,
                error = CSErrorReasons.SOCKET_NOT_OPEN,
                appVersion = info.appInfo.appVersion
            )
        )
        return null
    }

    private suspend fun isNetworkAvailable(): String? {
        logHealthEvent(
            clickstream.health.CSHealthEvent(
                eventName = ClickStreamEventBatchTriggerFailed.value,
                eventType = clickstream.health.EventTypes.AGGREGATE,
                error = CSErrorReasons.NETWORK_UNAVAILABLE,
                appVersion = info.appInfo.appVersion
            )
        )
        return null
    }

    private suspend fun isBatteryLow(): String? {
        logHealthEvent(
            clickstream.health.CSHealthEvent(
                eventName = ClickStreamEventBatchTriggerFailed.value,
                eventType = clickstream.health.EventTypes.AGGREGATE,
                error = CSErrorReasons.LOW_BATTERY,
                appVersion = info.appInfo.appVersion
            )
        )
        return null
    }
}
